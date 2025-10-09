package ex.org.project.userservice.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import ex.org.project.userservice.auth.AccessRole;
import ex.org.project.userservice.auth.UserNotFoundException;
import ex.org.project.userservice.dto.*;
import ex.org.project.userservice.entity.*;
import ex.org.project.userservice.exception.*;
import ex.org.project.userservice.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import ex.org.project.userservice.mapper.InstitutionMapper;
import ex.org.project.userservice.mapper.LookupCountryMapper;
import ex.org.project.userservice.mapper.LookupStateMapper;
import ex.org.project.userservice.mapper.UserMapper;
import ex.org.project.userservice.mapper.UserRegistrationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private final UserMapper userMapper;
	private final InstitutionMapper institutionMapper;
	private final UserRegistrationMapper userRegistrationMapper;
	private final LookupStateMapper lookupStateMapper;
	private final LookupCountryMapper lookupCountryMapper;
	private final UserRepository userRepository;
	private final LookupResearcherLevelRepository lookupResearcherLevelRepository;
	private final InstitutionRepository institutionRepository;
	private final LookupInstitutionTypeRepository lookupInstitutionTypeRepository;
	private final LookupStatusRepository lookupStatusRepository;
	private final LookupCountryRepository lookupCountryRepository;
	private final LookupStateRepository lookupStateRepository;
	private final LookupRoleRepository lookupRoleRepository;
	private final LkupCenterRepository centerRepository;
	private final MessageService messageService;
	private final LkupReferrerRepository lkupReferrerRepository;
	private final UserReferrerRepository userReferrerRepository;

	public UserDTO getUserInfo(String emailAddress) throws UserNotFoundException {
		User user = userRepository.findByEmail(emailAddress)
				.orElseThrow(() -> new UserNotFoundException("Unable to find user with provided email"));
		return userMapper.toUserDto(user);
	}

	@Transactional(readOnly = true)
	public List<InstitutionDTO> getApprovedInstitutions() {
		List<Institution> approvedInstitutions = institutionRepository.findByStatus_NameOrderByName("active");
		return institutionMapper.toInstitutionDTOs(approvedInstitutions);
	}

	@Transactional
	public InstitutionDTO createInstitution(InstitutionDTO institutionDTO) throws InstitutionCreationException {
		if(institutionRepository.existsByName(institutionDTO.getName())) {
			throw new InstitutionCreationException("Institution already exists");
		}
		LookupInstitutionType institutionType = lookupInstitutionTypeRepository.findByName(institutionDTO.getType())
				.orElseThrow(() -> new InstitutionCreationException("Invalid institution type"));

		LookupStatus pendingStatus = lookupStatusRepository.findByNameAndUsage("pending", "institution")
				.orElseThrow(() -> new StatusNotFoundException("Status not found: pending"));
		LookupCountry country = lookupCountryRepository.findByName(institutionDTO.getCountry())
				.orElseThrow(() -> new InstitutionCreationException("Invalid country"));

		LookupState state = null;
		if(institutionDTO.getState() != null && !institutionDTO.getState().trim().isBlank()) {
			state = lookupStateRepository.findByName(institutionDTO.getState())
					.orElseThrow(() -> new InstitutionCreationException("Invalid state"));
		}

		Institution institution = new Institution(institutionDTO, pendingStatus, institutionType, country, state);
		institution.setCreatedBy(9999);
		Institution savedInstitution = institutionRepository.save(institution);
		return institutionMapper.toInstitutionDto(savedInstitution);
	}

	@Transactional
	public UserRegistrationDTO saveUserRegistrationForm(Integer userId, UserRegistrationDTO userRegistrationDTO)
			throws UserInfoException {
		// Check if email already exists
		if(userRegistrationDTO.getEmail() != null && userRepository.existsByEmail(userRegistrationDTO.getEmail())){
			throw new UserRegistrationFormException("Account already exists");
		}

		// If authenticated (userId is not null), link to existing user or update
		if(userId != null) {
			User existingUser = userRepository.findById(userId).orElse(null);
			if(existingUser != null && userRepository.existsByEmail(existingUser.getEmail())){
				throw new UserRegistrationFormException("Account already exists");
			}
		}

		Institution institution = institutionRepository.findByName(userRegistrationDTO.getInstitution())
				.orElseThrow(() -> new UserRegistrationFormException("Invalid Institution"));

		LookupStatus pendingStatus = lookupStatusRepository.findByNameAndUsage("active", "general")
				.orElseThrow(() -> new StatusNotFoundException("Status not found: active"));

		LookupResearcherLevel researcherLevel = lookupResearcherLevelRepository.findByName(userRegistrationDTO.getResearcherLevel())
				.orElseThrow(() -> new UserRegistrationFormException("Invalid researcher level"));

		Set<Integer> referrerIds = lkupReferrerRepository.findAll()
				.stream()
				.map(LkupReferrer::getId)
				.collect(Collectors.toSet());
		List<Integer> providedReferrerIds = userRegistrationDTO.getReferrers()
				.stream()
				.map(ReferrerSelectionDTO::getReferrerId)
				.toList();
		if(!referrerIds.containsAll(providedReferrerIds)){
			throw new UserRegistrationFormException("Invalid Referrer ID(s)");
		}

		// Create the User entity
		User createdUser = new User(userRegistrationDTO, institution, pendingStatus,  researcherLevel);

		// Save the User entity
		User savedUser = userRepository.save(createdUser);
		int savedUserId = savedUser.getId();
		List<UserReferrer> referrerList = userRegistrationDTO.getReferrers()
				.stream()
				.map(referrer -> new UserReferrer(referrer, savedUserId))
				.toList();
		userReferrerRepository.saveAll(referrerList);

		messageService.sendWelcomeEmail(savedUser);
		// Map the saved User back to UserRegistrationDTO
		return userRegistrationMapper.toUserRegistrationtDto(savedUser);
	}

	public List<LkupReferrerDTO> getReferrerTypes() {
		List<LkupReferrer> referrerList = lkupReferrerRepository.findAllByOrderByDisplayOrderAsc();
		return userRegistrationMapper.mapReferrerTypes(referrerList) ;
	}

	public List<String> getResearcherLevels() {
		return lookupResearcherLevelRepository.findAll().stream().map(LookupResearcherLevel::getName).toList();
	}

	public List<String> getAllInstitutionTypes() {
		return lookupInstitutionTypeRepository.findAll().stream().map(LookupInstitutionType::getName).toList();
	}

	public List<LookupStateDTO> getAllStates() {
		return lookupStateRepository.findAllByOrderByDisplayOrder()
				.stream()
				.map(lookupStateMapper::toStateDto)
				.toList();
	}

	public List<LookupCountryDTO> getAllCountries() {
		return lookupCountryRepository.findAllByOrderByDisplayOrder()
				.stream()
				.map(lookupCountryMapper::toCountryDto)
				.toList();
	}

	public List<LkupCenter> getAllCenters(){
		return centerRepository.findAll();
	}

	public List<UserDTO> getUsersByStatus(String status) {
		LookupStatus lookupStatus = lookupStatusRepository.findByNameAndUsage(status, "general")
				.orElseThrow(()  -> new StatusNotFoundException(String.format("Invalid User Request Status: %s", status)));
		List<User> users = userRepository.findByStatusOrderByCreatedAtDesc(lookupStatus);
		return userMapper.toUserDTOs(users);
	}

	public List<Role> getAllRoles(){
		return lookupRoleRepository.findAll();
	}

	public List<String> getGeneralStatus() {
		return lookupStatusRepository.findByUsage("general")
				.stream()
				.map(LookupStatus::getName)
				.toList();
	}

	public UserDTO getUserInfoById(Integer id) throws UserNotFoundException {
		User user = userRepository.findById(id)
				.orElseThrow(() -> new UserNotFoundException("User ID not found"));
		return userMapper.toUserDto(user);
	}

	@Transactional
	public UserDTO updateUserInfo(Integer id, UserDTO userDTO) throws SubmitterCenterException, UserInfoException {
		User user = userRepository.findById(id)
				.orElseThrow(() -> new UserNotFoundException("Unable to find user with provided ID"));

		boolean isSubmitter = userDTO.getRoles()
				.stream()
				.anyMatch(role -> role.equals(AccessRole.DATA_SUBMITTER.label));
		if(isSubmitter && (userDTO.getCenter() == null || userDTO.getCenter().isBlank())) {
			throw new SubmitterCenterException("Provided Center field is blank.");
		}

		List<Role> roles = lookupRoleRepository.findAllByNameIn(userDTO.getRoles());
		if(roles.size() != userDTO.getRoles().size()) {
			log.warn("Supplied roles list size differs from retrieved Roles list. User ID: " + id);
		}

		Institution institution = institutionRepository.findByName(userDTO.getInstitution())
				.orElseThrow(() -> new UserInfoException("No institution found with provided name"));
		LookupStatus status = lookupStatusRepository.findLookupStatusByNameAndUsage(userDTO.getStatus(), "general")
				.orElseThrow(() -> new UserInfoException("Invalid user status"));
		LookupResearcherLevel researcherLevel = lookupResearcherLevelRepository.findByName(userDTO.getResearcherLevel())
				.orElseThrow(() -> new UserInfoException("Invalid researcher level"));

		LkupCenter center = null;
		if(userDTO.getCenter() != null && !userDTO.getCenter().isBlank()) {
			center = centerRepository.findByName(userDTO.getCenter())
					.orElseThrow(() -> new UserInfoException("DCC not found"));
		}

		user.updateUser(userDTO, institution, roles, status, researcherLevel, center);
		return userMapper.toUserDto(user);
	}

	@Transactional
	public UserDTO editProfile(Integer id, UserDTO userDTO) throws SubmitterCenterException, UserInfoException {
		User user = userRepository.findById(id)
				.orElseThrow(() -> new UserNotFoundException("Unable to find user with provided ID"));

		LookupResearcherLevel researcherLevel = lookupResearcherLevelRepository.findByName(userDTO.getResearcherLevel())
				.orElseThrow(() -> new UserInfoException("Invalid researcher level"));

		Institution institution;
		if(user.getEmail().contains("@nih.gov")){
			institution = user.getInstitution();
		} else {
			institution = institutionRepository.findByName(userDTO.getInstitution())
					.orElseThrow(() -> new UserInfoException("No institution found with provided name"));
		}
		user.updateUser(userDTO.getJobTitle(), userDTO.getOrcidId(), institution, researcherLevel);
		return userMapper.toUserDto(user);
	}
}
