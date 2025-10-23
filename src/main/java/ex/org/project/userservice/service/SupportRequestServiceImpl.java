package ex.org.project.userservice.service;

import ex.org.project.datahub.auth.exception.UserNotFoundException;
import ex.org.project.datahub.auth.model.AccessRole;
import ex.org.project.userservice.dto.EmailRequest;
import ex.org.project.userservice.dto.SupportAssigneeDTO;
import ex.org.project.userservice.dto.SupportRequestDTO;
import ex.org.project.userservice.dto.UserDTO;
import ex.org.project.userservice.entity.*;
import ex.org.project.userservice.exception.*;
import ex.org.project.userservice.mapper.SupportAssigneeMapper;
import ex.org.project.userservice.mapper.SupportRequestMapper;
import ex.org.project.userservice.repository.*;
import ex.org.project.userservice.util.EmailRequestType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileWriter;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SupportRequestServiceImpl implements SupportRequestService {

	private final SupportRequestRepository supportRequestRepository;
	private final LookupSupportRequestTypeRepository lookupSupportRequestTypeRepository;
	private final LookupStatusRepository lookupStatusRepository;
	private final ResolutionTypeRepository resolutionTypeRepository;
	private final UserService userService;
	private final UserRoleRepository userRoleRepository;
	private final SupportRequestMapper supportRequestMapper;
	private final SupportAssigneeMapper supportAssigneeMapper;
	private final MessageService messageService;

	private static final String SUPPORT_REQUEST = "support_request";
	private static final String INITIATED = "initiated";
	private static final String IN_PROGRESS = "in_progress";
	private static final String CLOSED = "closed";

    @Override
    public List<String> getSupportRequestTypeNames() {
        return lookupSupportRequestTypeRepository.findAll()
                .stream()
                .distinct()
                .map(LookupSupportRequestType::getName)
                .toList();
    }

	/**
	 * Saves a new support request and sends an email informing the user it has been opened
	 * @param supportRequestDTO object containing the information needed to open the support request
	 * @return SupportRequestDTO object containing the updated information about the opened support request
	 * @throws SupportRequestTypeNotFoundException thrown if the support request type doesn't exist
	 */
	@Transactional
	public SupportRequestDTO saveSupportRequest(SupportRequestDTO supportRequestDTO) {
		if(supportRequestDTO.isInvalidForInitialSave()) {
			throw new BadRequestException("Support request is missing required fields");
		}
		LookupSupportRequestType lookupType = lookupSupportRequestTypeRepository.findByName(supportRequestDTO.getRequestType())
				.orElseThrow(() -> new SupportRequestTypeNotFoundException(
						String.format("Invalid Support Request Type: %s", supportRequestDTO.getRequestType())));
		LookupStatus initiatedStatus = lookupStatusRepository.findByNameAndUsage(INITIATED, SUPPORT_REQUEST)
				.orElseThrow(() -> new StatusNotFoundException(
						String.format("Invalid Support Request Status: %s", INITIATED)));

		SupportRequest supportRequest = new SupportRequest(supportRequestDTO, initiatedStatus, lookupType);
		supportRequest = supportRequestRepository.save(supportRequest);

		EmailRequest emailRequest = messageService.createUserSupportEmailRequest(
				supportRequest,
				EmailRequestType.SUPPORT_REQUEST_SUBMISSION
		);
		messageService.sendMessage(emailRequest);

		return supportRequestMapper.toSupportRequestDto(supportRequest);
	}

	@Override
	public SupportRequestDTO getSupportRequestById(Integer id, Boolean isStaff) {
		SupportRequest supportRequest = findSupportRequestById(id);
		SupportRequestDTO supportRequestDTO = supportRequestMapper.toSupportRequestDto(supportRequest);
		supportRequestDTO.setCanEdit(isStaff);
		if(supportRequestDTO.getRequestorUserId() == null){
			return supportRequestDTO;
		}
		UserDTO user = userService.getUserInfoById(supportRequestDTO.getRequestorUserId());
		supportRequestDTO.setInstitution(user.getInstitution());
		supportRequestDTO.setFullName(String.format("%s %s", user.getFirstName(), user.getLastName()));
		supportRequestDTO.setEmail(user.getEmail());
		return supportRequestDTO;
	}

	private SupportRequest findSupportRequestById(Integer id){
		return supportRequestRepository.findById(id)
				.orElseThrow(() -> new SupportRequestNotFoundException("Support request ID not found"));
	}

	@Override
	public List<SupportRequestDTO> getAllSupportRequestsByStatus(String status) throws StatusNotFoundException {
		LookupStatus lookupStatus = lookupStatusRepository.findByNameAndUsage(status, SUPPORT_REQUEST)
				.orElseThrow(()  -> new StatusNotFoundException(String.format("Invalid Support Request Status: %s", status)));
		List<SupportRequest> supportRequests = supportRequestRepository.findByStatusOrderByCreatedAtDesc(lookupStatus);
		return supportRequestMapper.toDTOs(supportRequests);
	}

	@Override
	public List<SupportRequestDTO> getAllSupportRequests(){
		List<SupportRequest> fetchedSupportRequests = supportRequestRepository.findByStatusUsageOrderByCreatedAtDesc(SUPPORT_REQUEST);
		return supportRequestMapper.toDTOs(fetchedSupportRequests);
	}

	/**
	 * Updates a support request. Sends any necessary emails based on status changes or ticket assignment
	 * @param id the id of the support request to be updated
	 * @param supportRequestDTO object containing the information to be updated on the corresponding support request
	 * @return SupportRequestDTO object containing the updated information about the support request
	 */
	@Transactional
	public SupportRequestDTO updateSupportRequest(Integer userId, Integer id, SupportRequestDTO supportRequestDTO) {
		//TODO: remove id from params, just have it in the DTO
		SupportRequest supportRequest = findSupportRequestById(id);
		// providing a resolution type will close the support request
		boolean wasClosed = updateStatus(supportRequest, supportRequestDTO);
		updateRequestType(supportRequest, supportRequestDTO);
		boolean wasAssigned = updateAssignee(supportRequest, supportRequestDTO);
		if(supportRequestDTO.getNotes() != null){
			supportRequest.setNotes(supportRequestDTO.getNotes());
		}
		if(supportRequestDTO.getSeverity() != null){
			supportRequest.setSeverity(supportRequestDTO.getSeverity());
		}
		supportRequest.setUpdatedBy(userId);
		supportRequest = supportRequestRepository.save(supportRequest);

		if(wasClosed){
			EmailRequest emailRequest = messageService.createUserSupportEmailRequest(supportRequest, EmailRequestType.SUPPORT_REQUEST_CLOSED);
			messageService.sendMessage(emailRequest);
		}
		else if(wasAssigned){
			EmailRequest emailRequest = messageService.createSupportAssignmentEmailRequest(
					supportRequest,
					supportRequest.getAssigneeName()
			);
			messageService.sendMessage(emailRequest);
		}
		return supportRequestMapper.toSupportRequestDto(supportRequest);
	}

	private boolean isEmpty(String field) {
		return field == null || field.isBlank();
	}

	private <T> boolean isUnchangedValue(T oldValue, T newValue) {
		if(oldValue == null) {
            return newValue == null;
		} else {
			return oldValue.equals(newValue);
		}
	}

	private boolean updateStatus(SupportRequest request, SupportRequestDTO dto) {
		if(isEmpty(dto.getStatus())) {
			return updateResolutionTypeIfValid(request, dto, false);
		}
		LookupStatus status = lookupStatusRepository.findByNameAndUsage(dto.getStatus(), SUPPORT_REQUEST)
				.orElseThrow(() -> new StatusNotFoundException("Invalid Support Request Status"));
		boolean isClosed = status.getName().equals(CLOSED) || !isEmpty(dto.getResolutionType());
		if(isUnchangedValue(request.getStatus(), status) || isClosed) {
			return updateResolutionTypeIfValid(request, dto, isClosed);
		}
		request.setStatus(status);
		return false;
	}

	private boolean updateResolutionTypeIfValid(SupportRequest request, SupportRequestDTO dto, boolean requiresResolution) {
		boolean wasClosed = false;
		if(isEmpty(dto.getResolutionType())){
			if(requiresResolution) {
				throw new BadRequestException("Resolution type is required");
			}
			return wasClosed;
		}
		ResolutionType resolution = resolutionTypeRepository.findByName(dto.getResolutionType())
				.orElseThrow(() -> new SupportRequestTypeNotFoundException("Invalid resolution type"));
		if(isUnchangedValue(request.getResolutionType(), resolution)) {
			return wasClosed;
		}
		// email should only be sent if resolution type is being set for the first time
		if(request.getResolutionType() == null) {
			LookupStatus closedStatus = lookupStatusRepository.findByNameAndUsage(CLOSED, SUPPORT_REQUEST)
					.orElseThrow(() -> new BadDataException(String.format("Invalid Support Request Status: %s", CLOSED)));
			request.setStatus(closedStatus);
			wasClosed = true;
		}
		request.setResolutionType(resolution);
		request.setResolvedAt(ZonedDateTime.now());
		return wasClosed;
	}

	private boolean updateRequestType(SupportRequest request, SupportRequestDTO dto) {
		if(isEmpty(dto.getRequestType())) {
			return false;
		}
		LookupSupportRequestType type = lookupSupportRequestTypeRepository.findByName(dto.getRequestType())
				.orElseThrow(() -> new SupportRequestTypeNotFoundException("Invalid Support Request Type"));
		if(isUnchangedValue(request.getRequestType(), type)) {
			return false;
		}
		request.setRequestType(type);
		return true;
	}

	private boolean updateAssignee(SupportRequest request, SupportRequestDTO dto) {
		if(dto.getAssigneeUserId() == null){
			return false;
		}
		UserDTO assignee = userService.getUserInfoById(dto.getAssigneeUserId());
		if(!assignee.getRoles().contains(AccessRole.SUPPORT_TEAM.label)) {
			String errorMessage = "No support team member found";
			log.warn(String.format("%s for ID: %d", errorMessage, assignee.getId()));
			throw new UserNotFoundException(errorMessage);
		}
		if(isUnchangedValue(request.getAssigneeUserId(), assignee.getId())) {
			return false;
		}
		request.setAssigneeUserId(assignee.getId());
		request.setAssigneeEmail(assignee.getEmail());
		request.setAssignedAt(ZonedDateTime.now());
		request.setAssigneeName(String.format("%s %s", assignee.getFirstName(), assignee.getLastName()));
		return true;
	}

	@Override
	public List<Integer> getAllSeverities() {
		return Arrays.asList(1, 2, 3, 4, 5);
	}

	@Override
	public List<String> getAllResolutionTypes() {
		List<ResolutionType> validResolutionTypes = resolutionTypeRepository.findAll();
		return validResolutionTypes.stream().map(ResolutionType::getName).collect(Collectors.toList());
	}

	@Override
	public List<String> getAllStatuses() {
		List<LookupStatus> allStatuses = lookupStatusRepository.findByUsage(SUPPORT_REQUEST);
		return allStatuses.stream().map(LookupStatus::getName).collect(Collectors.toList());
	}

	@Override
	public List<SupportAssigneeDTO> getAllAssignees(){
		List<UserRole> supportUsers = userRoleRepository.findByRole_Name("Support Team");
		return supportAssigneeMapper.toDTOs(supportUsers);
	}

	public String downloadSupportRequestReportsToCSV() throws IOException {

		String filepath = "support_request.csv";

		List<SupportRequest> requests = supportRequestRepository.findAll();

		try (FileWriter writer = new FileWriter(filepath);
				CSVPrinter csvPrinter = new CSVPrinter(writer,
						CSVFormat.DEFAULT.withHeader("id", "request_type", "full_name", "email", "request_title",
								"request_detail", "assignee_id", "asignee_email", "assigned_at", "resolved_at",
								"resolution_type", "notes", "created_at", "updated_at"))) {
			for (SupportRequest request : requests) {
				csvPrinter.printRecord(request.getId(), request.getRequestType().getName(), request.getFullName(),
						request.getEmail(), request.getRequestTitle(), request.getRequestDetail(),
						request.getAssigneeUserId(), request.getAssigneeEmail(), request.getAssignedAt(),
						request.getResolvedAt(),
						(request.getResolutionType() != null ? request.getResolutionType().getName() : ""),
						request.getNotes(), request.getCreatedAt(), request.getUpdateAt());
			}

		}
		return filepath;
	}

	@Override
	public SupportRequestDTO saveLoggedInUserSupportRequest(SupportRequestDTO supportRequestDTO, Integer userId) {
		supportRequestDTO.setRequestorUserId(userId);
		return saveSupportRequest(supportRequestDTO);
	}
}
