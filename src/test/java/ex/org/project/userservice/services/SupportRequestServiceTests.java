package ex.org.project.userservice.services;

import ex.org.project.userservice.auth.AccessRole;
import ex.org.project.userservice.auth.UserNotFoundException;
import ex.org.project.userservice.dto.EmailRequest;
import ex.org.project.userservice.dto.SupportRequestDTO;
import ex.org.project.userservice.dto.UserDTO;
import ex.org.project.userservice.entity.LookupStatus;
import ex.org.project.userservice.entity.LookupSupportRequestType;
import ex.org.project.userservice.entity.ResolutionType;
import ex.org.project.userservice.entity.SupportRequest;
import ex.org.project.userservice.exception.BadRequestException;
import ex.org.project.userservice.exception.SupportRequestNotFoundException;
import ex.org.project.userservice.mapper.SupportAssigneeMapper;
import ex.org.project.userservice.mapper.SupportRequestMapper;
import ex.org.project.userservice.mapper.SupportRequestMapperImpl;
import ex.org.project.userservice.repository.*;
import ex.org.project.userservice.service.MessageService;
import ex.org.project.userservice.service.SupportRequestServiceImpl;
import ex.org.project.userservice.service.UserService;
import ex.org.project.userservice.util.EmailRequestType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class SupportRequestServiceTests {

    @Mock
    private SupportRequestRepository supportRequestRepository;
    @Mock
    private LookupSupportRequestTypeRepository lookupSupportRequestTypeRepository;
    @Mock
    private LookupStatusRepository lookupStatusRepository;
    @Mock
    private ResolutionTypeRepository resolutionTypeRepository;
    @Mock
    private UserService userService;
    @Mock
    private UserRoleRepository userRoleRepository;
    @Spy
    private SupportRequestMapper supportRequestMapper = new SupportRequestMapperImpl();
    @Mock
    private SupportAssigneeMapper supportAssigneeMapper;
    @Mock
    private MessageService messageService;

    @InjectMocks
    private SupportRequestServiceImpl supportRequestService;

    @Captor
    ArgumentCaptor<SupportRequest> requestCaptor;

    @Test
    void testSaveSupportRequest_ValidDto(){
        SupportRequestDTO dto = new SupportRequestDTO();
        dto.setFullName("Test McTestington");
        dto.setEmail("test@bah.com");
        dto.setInstitution("BAH");
        dto.setRequestTitle("Test");
        dto.setRequestType("Technical");
        dto.setRequestDetail("Test");

        when(lookupSupportRequestTypeRepository.findByName("Technical"))
                .thenReturn(Optional.of(new LookupSupportRequestType()));
        when(lookupStatusRepository.findByNameAndUsage(anyString(), anyString()))
                .thenReturn(Optional.of(new LookupStatus()));
        when(messageService.createUserSupportEmailRequest(any(), eq(EmailRequestType.SUPPORT_REQUEST_SUBMISSION)))
                .thenReturn(mock(EmailRequest.class));
        when(supportRequestRepository.save(any(SupportRequest.class)))
                .thenAnswer(i -> i.getArguments()[0]);

        SupportRequestDTO response = supportRequestService.saveSupportRequest(dto);

        verify(messageService, times(1)).sendMessage(any(EmailRequest.class));
        assertEquals(response.getFullName(), dto.getFullName());
        assertEquals(response.getEmail(), dto.getEmail());
        assertEquals(response.getInstitution(), dto.getInstitution());
        assertEquals(response.getRequestDetail(), dto.getRequestDetail());
        assertEquals(response.getRequestTitle(), dto.getRequestTitle());
    }

    @Test
    void testSaveSupportRequest_InvalidDto(){
        SupportRequestDTO dto = new SupportRequestDTO();
        assertThrows(BadRequestException.class, () -> supportRequestService.saveSupportRequest(dto));
    }

    private SupportRequest getMockSupportRequest(){
        SupportRequest request = new SupportRequest();
        request.setId(1);
        request.setFullName("Test McTestington");
        request.setEmail("test@bah.com");
        request.setRequestTitle("Test");
        request.setRequestDetail("Details");
        LookupStatus status = new LookupStatus();
        status.setId(1);
        status.setName("initiated");
        request.setStatus(status);
        LookupSupportRequestType requestType = new LookupSupportRequestType();
        requestType.setId(1);
        requestType.setName("Technical");
        request.setRequestType(requestType);
        request.setInstitution("BAH");
        request.setUpdatedBy(1);
        return request;
    }

    @Test
    void testGetSupportRequestById_WithUserId(){
        SupportRequest request = getMockSupportRequest();
        request.setRequestorUserId(1);
        UserDTO userDTO = new UserDTO();
        userDTO.setInstitution("Official BAH");
        userDTO.setFirstName("Official Test");
        userDTO.setLastName("McTestington");
        userDTO.setEmail("test@bah.com");

        when(supportRequestRepository.findById(1))
                .thenReturn(Optional.of(request));
        when(userService.getUserInfoById(1))
                .thenReturn(userDTO);

        SupportRequestDTO response = supportRequestService.getSupportRequestById(1, false);

        assertEquals(request.getId(), response.getId());
        assertEquals("Official Test McTestington", response.getFullName());
    }

    @Test
    void testGetSupportRequestById_WithoutUserId(){
        SupportRequest request = getMockSupportRequest();

        when(supportRequestRepository.findById(1))
                .thenReturn(Optional.of(request));

        SupportRequestDTO response = supportRequestService.getSupportRequestById(1, false);

        assertEquals(request.getId(), response.getId());
        assertEquals("Test McTestington", response.getFullName());
    }

    @Test
    void testGetSupportRequestById_NotFound(){
        when(supportRequestRepository.findById(anyInt()))
                .thenReturn(Optional.empty());
        assertThrows(SupportRequestNotFoundException.class,
                     () -> supportRequestService.getSupportRequestById(1, false));
    }

    @Test
    void testUpdateSupportRequest_AllDtoFieldsNull(){
        SupportRequestDTO dto = new SupportRequestDTO();
        Integer userId = 2;
        Integer supportRequestId = 1;
        SupportRequest request = getMockSupportRequest();

        when(supportRequestRepository.findById(supportRequestId))
                .thenReturn(Optional.of(request));

        SupportRequestDTO response = supportRequestService.updateSupportRequest(userId, supportRequestId, dto);

        verify(supportRequestRepository).save(requestCaptor.capture());
        verify(messageService, times(0)).sendMessage(any());
        SupportRequest capturedSavedRequest = requestCaptor.getValue();

        assertEquals(request.getStatus(), capturedSavedRequest.getStatus());
        assertEquals("initiated", capturedSavedRequest.getStatus().getName());
        assertEquals(request.getRequestType(), capturedSavedRequest.getRequestType());
        assertEquals("Technical", capturedSavedRequest.getRequestType().getName());
        assertEquals(request.getResolutionType(), capturedSavedRequest.getResolutionType());
        assertNull(capturedSavedRequest.getResolutionType());
        assertEquals(request.getSeverity(), capturedSavedRequest.getSeverity());
        assertNull(capturedSavedRequest.getSeverity());
        assertEquals(request.getNotes(), capturedSavedRequest.getNotes());
        assertNull(capturedSavedRequest.getNotes());
        assertEquals(request.getAssigneeUserId(), capturedSavedRequest.getAssigneeUserId());
        assertNull(capturedSavedRequest.getAssigneeUserId());
    }

    @Test
    void testUpdateSupportRequest_RequestNotFound(){
        SupportRequestDTO dto = new SupportRequestDTO();
        Integer userId = 2;
        Integer supportRequestId = 1;

        when(supportRequestRepository.findById(1))
                .thenReturn(Optional.empty());

        assertThrows(SupportRequestNotFoundException.class,
                     () -> supportRequestService.updateSupportRequest(userId, supportRequestId, dto));
    }

    @Test
    void testUpdateSupportRequest_InProgressStatus(){
        SupportRequestDTO dto = new SupportRequestDTO();
        dto.setStatus("in_progress");
        LookupStatus status = new LookupStatus();
        status.setId(1);
        status.setName("in_progress");
        status.setUsage("support_request");
        Integer userId = 2;
        Integer supportRequestId = 1;
        SupportRequest request = getMockSupportRequest();
        String oldStatus = request.getStatus().getName();

        when(supportRequestRepository.findById(supportRequestId))
                .thenReturn(Optional.of(request));
        when(lookupStatusRepository.findByNameAndUsage("in_progress", "support_request"))
                .thenReturn(Optional.of(status));

        SupportRequestDTO response = supportRequestService.updateSupportRequest(userId, supportRequestId, dto);

        verify(supportRequestRepository).save(requestCaptor.capture());
        SupportRequest capturedSavedRequest = requestCaptor.getValue();
        verify(messageService, times(0)).sendMessage(any());

        assertNotEquals(oldStatus, capturedSavedRequest.getStatus().getName());
        assertEquals("in_progress", capturedSavedRequest.getStatus().getName());
        assertEquals(request.getResolutionType(), capturedSavedRequest.getResolutionType());
    }

    @Test
    void testUpdateSupportRequest_EmptyStatus_ValidResolution(){
        SupportRequestDTO dto = new SupportRequestDTO();
        String typeName = "User Error";
        dto.setResolutionType(typeName);
        ResolutionType resolutionType = new ResolutionType();
        resolutionType.setId(1);
        resolutionType.setName(typeName);
        LookupStatus status = new LookupStatus();
        status.setId(1);
        status.setName("closed");
        status.setUsage("support_request");
        Integer userId = 2;
        Integer supportRequestId = 1;
        SupportRequest request = getMockSupportRequest();
        String oldStatus = request.getStatus().getName();
        String oldResolution;
        if(request.getResolutionType() == null) {
            oldResolution = null;
        } else {
            oldResolution = request.getResolutionType().getName();
        }

        when(supportRequestRepository.findById(supportRequestId))
                .thenReturn(Optional.of(request));
        when(resolutionTypeRepository.findByName(typeName))
                .thenReturn(Optional.of(resolutionType));
        when(lookupStatusRepository.findByNameAndUsage("closed", "support_request"))
                .thenReturn(Optional.of(status));
        when(supportRequestRepository.save(any(SupportRequest.class)))
                .thenAnswer(i -> i.getArguments()[0]);

        SupportRequestDTO response = supportRequestService.updateSupportRequest(userId, supportRequestId, dto);

        verify(supportRequestRepository).save(requestCaptor.capture());
        SupportRequest capturedSavedRequest = requestCaptor.getValue();
        verify(messageService, times(1)).sendMessage(any());

        assertNotEquals(oldStatus, capturedSavedRequest.getStatus().getName());
        assertEquals("closed", capturedSavedRequest.getStatus().getName());
        assertNotEquals(oldResolution, capturedSavedRequest.getResolutionType().getName());
        assertEquals("User Error", capturedSavedRequest.getResolutionType().getName());
    }

    @Test
    void testUpdateSupportRequest_ClosedStatus_ValidResolution(){
        SupportRequestDTO dto = new SupportRequestDTO();
        String typeName = "User Error";
        dto.setResolutionType(typeName);
        dto.setStatus("closed");
        ResolutionType resolutionType = new ResolutionType();
        resolutionType.setId(1);
        resolutionType.setName(typeName);
        LookupStatus status = new LookupStatus();
        status.setId(1);
        status.setName("closed");
        status.setUsage("support_request");
        Integer userId = 2;
        Integer supportRequestId = 1;
        SupportRequest request = getMockSupportRequest();
        String oldStatus = request.getStatus().getName();
        String oldResolution;
        if(request.getResolutionType() == null) {
            oldResolution = null;
        } else {
            oldResolution = request.getResolutionType().getName();
        }

        when(supportRequestRepository.findById(supportRequestId))
                .thenReturn(Optional.of(request));
        when(resolutionTypeRepository.findByName(typeName))
                .thenReturn(Optional.of(resolutionType));
        when(lookupStatusRepository.findByNameAndUsage("closed", "support_request"))
                .thenReturn(Optional.of(status));
        when(supportRequestRepository.save(any(SupportRequest.class)))
                .thenAnswer(i -> i.getArguments()[0]);

        SupportRequestDTO response = supportRequestService.updateSupportRequest(userId, supportRequestId, dto);

        verify(supportRequestRepository).save(requestCaptor.capture());
        SupportRequest capturedSavedRequest = requestCaptor.getValue();
        verify(messageService, times(1)).sendMessage(any());

        assertNotEquals(oldStatus, capturedSavedRequest.getStatus().getName());
        assertEquals("closed", capturedSavedRequest.getStatus().getName());
        assertNotEquals(oldResolution, capturedSavedRequest.getResolutionType().getName());
        assertEquals("User Error", capturedSavedRequest.getResolutionType().getName());
    }

    @Test
    void testUpdateSupportRequest_ClosedStatus_EmptyResolution(){
        SupportRequestDTO dto = new SupportRequestDTO();
        dto.setStatus("closed");
        LookupStatus status = new LookupStatus();
        status.setId(1);
        status.setName("closed");
        status.setUsage("support_request");
        Integer userId = 2;
        Integer supportRequestId = 1;
        SupportRequest request = getMockSupportRequest();

        when(supportRequestRepository.findById(supportRequestId))
                .thenReturn(Optional.of(request));
        when(lookupStatusRepository.findByNameAndUsage("closed", "support_request"))
                .thenReturn(Optional.of(status));

        assertThrows(BadRequestException.class,
                     () -> supportRequestService.updateSupportRequest(userId, supportRequestId, dto));
    }

    @Test
    void testUpdateSupportRequest_EmptyStatus_SameResolution(){
        SupportRequestDTO dto = new SupportRequestDTO();
        dto.setResolutionType("User Error");
        ResolutionType resolutionType = new ResolutionType();
        resolutionType.setId(1);
        resolutionType.setName("User Error");
        LookupStatus status = new LookupStatus();
        status.setId(1);
        status.setName("closed");
        status.setUsage("support_request");
        Integer userId = 2;
        Integer supportRequestId = 1;
        SupportRequest request = getMockSupportRequest();
        request.setStatus(status);
        request.setResolutionType(resolutionType);
        String oldStatus = request.getStatus().getName();
        String oldResolution = request.getResolutionType().getName();
        ResolutionType resolutionTypeAlt = new ResolutionType();
        resolutionTypeAlt.setId(1);
        resolutionTypeAlt.setName("User Error");

        when(supportRequestRepository.findById(supportRequestId))
                .thenReturn(Optional.of(request));
        when(resolutionTypeRepository.findByName("User Error"))
                .thenReturn(Optional.of(resolutionTypeAlt));
        when(supportRequestRepository.save(any(SupportRequest.class)))
                .thenAnswer(i -> i.getArguments()[0]);

        SupportRequestDTO response = supportRequestService.updateSupportRequest(userId, supportRequestId, dto);

        verify(supportRequestRepository).save(requestCaptor.capture());
        SupportRequest capturedSavedRequest = requestCaptor.getValue();
        verify(messageService, times(0)).sendMessage(any());

        assertEquals(oldStatus, capturedSavedRequest.getStatus().getName());
        assertEquals("closed", capturedSavedRequest.getStatus().getName());
        assertEquals(oldResolution, capturedSavedRequest.getResolutionType().getName());
        assertEquals("User Error", capturedSavedRequest.getResolutionType().getName());
    }

    @Test
    void testUpdateSupportRequest_EmptyStatus_ResolutionPreviouslyNull(){
        SupportRequestDTO dto = new SupportRequestDTO();
        dto.setResolutionType("User Error");
        ResolutionType resolutionType = new ResolutionType();
        resolutionType.setId(1);
        resolutionType.setName("User Error");
        LookupStatus status = new LookupStatus();
        status.setId(1);
        status.setName("closed");
        status.setUsage("support_request");
        Integer userId = 2;
        Integer supportRequestId = 1;
        SupportRequest request = getMockSupportRequest();
        String oldStatus = request.getStatus().getName();
        String oldResolution;
        if(request.getResolutionType() == null) {
            oldResolution = null;
        } else {
            oldResolution = request.getResolutionType().getName();
        }

        when(supportRequestRepository.findById(supportRequestId))
                .thenReturn(Optional.of(request));
        when(resolutionTypeRepository.findByName("User Error"))
                .thenReturn(Optional.of(resolutionType));
        when(lookupStatusRepository.findByNameAndUsage("closed", "support_request"))
                .thenReturn(Optional.of(status));
        when(supportRequestRepository.save(any(SupportRequest.class)))
                .thenAnswer(i -> i.getArguments()[0]);

        SupportRequestDTO response = supportRequestService.updateSupportRequest(userId, supportRequestId, dto);

        verify(supportRequestRepository).save(requestCaptor.capture());
        SupportRequest capturedSavedRequest = requestCaptor.getValue();
        verify(messageService, times(1)).sendMessage(any());

        assertNotEquals(oldStatus, capturedSavedRequest.getStatus().getName());
        assertEquals("closed", capturedSavedRequest.getStatus().getName());
        assertNotEquals(oldResolution, capturedSavedRequest.getResolutionType().getName());
        assertEquals("User Error", capturedSavedRequest.getResolutionType().getName());
    }

    @Test
    void testUpdateSupportRequest_UnchangedStatus(){
        SupportRequestDTO dto = new SupportRequestDTO();
        dto.setStatus("initiated");
        LookupStatus status = new LookupStatus();
        status.setId(1);
        status.setName("initiated");
        status.setUsage("support_request");
        Integer userId = 2;
        Integer supportRequestId = 1;
        SupportRequest request = getMockSupportRequest();
        String oldStatus = request.getStatus().getName();
        ResolutionType oldResolution = null;

        when(supportRequestRepository.findById(supportRequestId))
                .thenReturn(Optional.of(request));
        when(lookupStatusRepository.findByNameAndUsage("initiated", "support_request"))
                .thenReturn(Optional.of(status));
        when(supportRequestRepository.save(any(SupportRequest.class)))
                .thenAnswer(i -> i.getArguments()[0]);

        SupportRequestDTO response = supportRequestService.updateSupportRequest(userId, supportRequestId, dto);

        verify(supportRequestRepository).save(requestCaptor.capture());
        SupportRequest capturedSavedRequest = requestCaptor.getValue();
        verify(messageService, times(0)).sendMessage(any());

        assertEquals(oldStatus, capturedSavedRequest.getStatus().getName());
        assertEquals(oldResolution, capturedSavedRequest.getResolutionType());
    }

    @Test
    void testUpdateSupportRequest_ValidRequestType(){
        SupportRequestDTO dto = new SupportRequestDTO();
        dto.setRequestType("Feature Request");
        LookupSupportRequestType requestType = new LookupSupportRequestType();
        requestType.setId(1);
        requestType.setName("Feature Request");
        Integer userId = 2;
        Integer supportRequestId = 1;
        SupportRequest request = getMockSupportRequest();
        String oldRequestType = request.getRequestType().getName();

        when(supportRequestRepository.findById(supportRequestId))
                .thenReturn(Optional.of(request));
        when(lookupSupportRequestTypeRepository.findByName("Feature Request"))
                .thenReturn(Optional.of(requestType));

        SupportRequestDTO response = supportRequestService.updateSupportRequest(userId, supportRequestId, dto);

        verify(supportRequestRepository).save(requestCaptor.capture());
        verify(messageService, times(0)).sendMessage(any());
        SupportRequest capturedSavedRequest = requestCaptor.getValue();

        assertNotEquals(oldRequestType, capturedSavedRequest.getRequestType().getName());
    }

    @Test
    void testUpdateSupportRequest_EmptyRequestType(){
        SupportRequestDTO dto = new SupportRequestDTO();
        dto.setRequestType("");
        LookupSupportRequestType requestType = new LookupSupportRequestType();
        requestType.setId(1);
        requestType.setName("Feature Request");
        Integer userId = 2;
        Integer supportRequestId = 1;
        SupportRequest request = getMockSupportRequest();
        String oldRequestType = request.getRequestType().getName();

        when(supportRequestRepository.findById(supportRequestId))
                .thenReturn(Optional.of(request));

        SupportRequestDTO response = supportRequestService.updateSupportRequest(userId, supportRequestId, dto);

        verify(supportRequestRepository).save(requestCaptor.capture());
        verify(messageService, times(0)).sendMessage(any());
        SupportRequest capturedSavedRequest = requestCaptor.getValue();

        assertEquals(oldRequestType, capturedSavedRequest.getRequestType().getName());
    }

    @Test
    void testUpdateSupportRequest_UnchangedRequestType(){
        SupportRequestDTO dto = new SupportRequestDTO();
        dto.setRequestType("Technical");
        LookupSupportRequestType requestType = new LookupSupportRequestType();
        requestType.setId(1);
        requestType.setName("Technical");
        Integer userId = 2;
        Integer supportRequestId = 1;
        SupportRequest request = getMockSupportRequest();
        String oldRequestType = request.getRequestType().getName();

        when(supportRequestRepository.findById(supportRequestId))
                .thenReturn(Optional.of(request));
        when(lookupSupportRequestTypeRepository.findByName("Technical"))
                .thenReturn(Optional.of(requestType));

        SupportRequestDTO response = supportRequestService.updateSupportRequest(userId, supportRequestId, dto);

        verify(supportRequestRepository).save(requestCaptor.capture());
        verify(messageService, times(0)).sendMessage(any());
        SupportRequest capturedSavedRequest = requestCaptor.getValue();

        assertEquals(oldRequestType, capturedSavedRequest.getRequestType().getName());
        assertEquals("Technical", capturedSavedRequest.getRequestType().getName());
    }

    private UserDTO getMockAssignee() {
        UserDTO assignee = new UserDTO();
        assignee.setId(1);
        assignee.setEmail("assignee@bah.com");
        assignee.setFirstName("Fred");
        assignee.setLastName("Flintstone");
        return assignee;
    }

    @Test
    void testUpdateSupportRequest_ValidAssignee(){
        SupportRequestDTO dto = new SupportRequestDTO();
        dto.setAssigneeUserId(1);
        Integer userId = 2;
        Integer supportRequestId = 1;
        SupportRequest request = getMockSupportRequest();
        UserDTO assignee = getMockAssignee();
        assignee.setRoles(List.of(AccessRole.SUPPORT_TEAM.label));
        Integer oldAssignee = request.getAssigneeUserId();

        when(supportRequestRepository.findById(supportRequestId))
                .thenReturn(Optional.of(request));
        when(userService.getUserInfoById(1))
                .thenReturn(assignee);
        when(supportRequestRepository.save(any(SupportRequest.class)))
                .thenAnswer(i -> i.getArguments()[0]);

        SupportRequestDTO response = supportRequestService.updateSupportRequest(userId, supportRequestId, dto);

        verify(supportRequestRepository).save(requestCaptor.capture());
        verify(messageService, times(1)).sendMessage(any());
        SupportRequest capturedSavedRequest = requestCaptor.getValue();

        assertNotEquals(oldAssignee, capturedSavedRequest.getAssigneeUserId());
        assertEquals(1, capturedSavedRequest.getAssigneeUserId());
    }

    @Test
    void testUpdateSupportRequest_AssigneeNotFound(){
        SupportRequestDTO dto = new SupportRequestDTO();
        dto.setAssigneeUserId(1);
        Integer userId = 2;
        Integer supportRequestId = 1;
        SupportRequest request = getMockSupportRequest();

        when(supportRequestRepository.findById(supportRequestId))
                .thenReturn(Optional.of(request));
        when(userService.getUserInfoById(1))
                .thenThrow(UserNotFoundException.class);

        assertThrows(UserNotFoundException.class,
                     () -> supportRequestService.updateSupportRequest(userId, supportRequestId, dto));
    }

    @Test
    void testUpdateSupportRequest_AssigneeNotSupportMember(){
        SupportRequestDTO dto = new SupportRequestDTO();
        dto.setAssigneeUserId(1);
        Integer userId = 2;
        Integer supportRequestId = 1;
        SupportRequest request = getMockSupportRequest();
        UserDTO assignee = getMockAssignee();
        assignee.setRoles(new ArrayList<>());

        when(supportRequestRepository.findById(supportRequestId))
                .thenReturn(Optional.of(request));
        when(userService.getUserInfoById(1))
                .thenReturn(assignee);

        assertThrows(UserNotFoundException.class,
                     () -> supportRequestService.updateSupportRequest(userId, supportRequestId, dto));
    }

    @Test
    void testUpdateSupportRequest_UnchangedAssignee(){
        SupportRequestDTO dto = new SupportRequestDTO();
        dto.setAssigneeUserId(1);
        Integer userId = 2;
        Integer supportRequestId = 1;
        SupportRequest request = getMockSupportRequest();
        request.setAssigneeUserId(1);
        UserDTO assignee = getMockAssignee();
        assignee.setRoles(List.of(AccessRole.SUPPORT_TEAM.label));
        Integer oldAssignee = request.getAssigneeUserId();

        when(supportRequestRepository.findById(supportRequestId))
                .thenReturn(Optional.of(request));
        when(userService.getUserInfoById(1))
                .thenReturn(assignee);
        when(supportRequestRepository.save(any(SupportRequest.class)))
                .thenAnswer(i -> i.getArguments()[0]);

        SupportRequestDTO response = supportRequestService.updateSupportRequest(userId, supportRequestId, dto);

        verify(supportRequestRepository).save(requestCaptor.capture());
        verify(messageService, times(0)).sendMessage(any());
        SupportRequest capturedSavedRequest = requestCaptor.getValue();

        assertEquals(oldAssignee, capturedSavedRequest.getAssigneeUserId());
        assertEquals(1, capturedSavedRequest.getAssigneeUserId());
    }

    @Test
    void testUpdateSupportRequest_Notes(){
        SupportRequestDTO dto = new SupportRequestDTO();
        dto.setNotes("notes");
        Integer userId = 2;
        Integer supportRequestId = 1;
        SupportRequest request = getMockSupportRequest();

        when(supportRequestRepository.findById(supportRequestId))
                .thenReturn(Optional.of(request));

        SupportRequestDTO response = supportRequestService.updateSupportRequest(userId, supportRequestId, dto);

        verify(supportRequestRepository).save(requestCaptor.capture());
        verify(messageService, times(0)).sendMessage(any());
        SupportRequest capturedSavedRequest = requestCaptor.getValue();

        assertEquals(request.getStatus(), capturedSavedRequest.getStatus());
        assertEquals(request.getRequestType(), capturedSavedRequest.getRequestType());
        assertEquals(request.getResolutionType(), capturedSavedRequest.getResolutionType());
        assertEquals(request.getSeverity(), capturedSavedRequest.getSeverity());
        assertEquals("notes", capturedSavedRequest.getNotes());
        assertEquals(request.getAssigneeUserId(), capturedSavedRequest.getAssigneeUserId());
    }

    @Test
    void testUpdateSupportRequest_Severity(){
        SupportRequestDTO dto = new SupportRequestDTO();
        dto.setSeverity(5);
        Integer userId = 2;
        Integer supportRequestId = 1;
        SupportRequest request = getMockSupportRequest();

        when(supportRequestRepository.findById(supportRequestId))
                .thenReturn(Optional.of(request));

        SupportRequestDTO response = supportRequestService.updateSupportRequest(userId, supportRequestId, dto);

        verify(supportRequestRepository).save(requestCaptor.capture());
        verify(messageService, times(0)).sendMessage(any());
        SupportRequest capturedSavedRequest = requestCaptor.getValue();

        assertEquals(request.getStatus(), capturedSavedRequest.getStatus());
        assertEquals(request.getRequestType(), capturedSavedRequest.getRequestType());
        assertEquals(request.getResolutionType(), capturedSavedRequest.getResolutionType());
        assertEquals(5, capturedSavedRequest.getSeverity());
        assertEquals(request.getNotes(), capturedSavedRequest.getNotes());
        assertEquals(request.getAssigneeUserId(), capturedSavedRequest.getAssigneeUserId());
    }

}
