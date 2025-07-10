package ex.org.project.userservice.services;

import ex.org.project.userservice.auth.AccessRole;
import ex.org.project.userservice.auth.UserAuthenticationException;
import ex.org.project.userservice.auth.UserNotFoundException;
import ex.org.project.userservice.auth.ras.AuthRasTracking;
import ex.org.project.userservice.auth.ras.AuthRasTrackingRepository;
import ex.org.project.userservice.dto.*;
import ex.org.project.userservice.entity.*;
import ex.org.project.userservice.exception.*;
import ex.org.project.userservice.mapper.*;
import ex.org.project.userservice.repository.*;
import ex.org.project.userservice.service.MessageService;
import ex.org.project.userservice.service.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTests {

    @Spy private UserMapper userMapper = new UserMapperImpl();
    @Spy private UserRegistrationMapper userRegistrationMapper = new UserRegistrationMapperImpl();
    @Spy private InstitutionMapper institutionMapper = new InstitutionMapperImpl();

    @Mock private LookupStateMapper lookupStateMapper;
    @Mock private LookupCountryMapper lookupCountryMapper;
    @Mock private UserRepository userRepository;
    @Mock private LookupResearcherLevelRepository lookupResearcherLevelRepository;
    @Mock private InstitutionRepository institutionRepository;
    @Mock private LookupInstitutionTypeRepository lookupInstitutionTypeRepository;
    @Mock private LookupStatusRepository lookupStatusRepository;
    @Mock private LookupCountryRepository lookupCountryRepository;
    @Mock private LookupStateRepository lookupStateRepository;
    @Mock private LookupRoleRepository lookupRoleRepository;
    @Mock private AuthRasTrackingRepository rasTrackingRepository;
    @Mock private LkupDCCRepository dccRepository;
    @Mock private MessageService messageService;
    @Mock private LkupReferrerRepository referrerRepository;
    @Mock private UserReferrerRepository userReferrerRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private UserDTO getUserDto() {
        UserDTO userDto = new UserDTO();
        userDto.setFirstName("Test");
        userDto.setLastName("McTestington");
        userDto.setEmail("test@bah.com");
        userDto.setInstitution("Booz Allen Hamilton");
        userDto.setDcc("RADx Tech");
        userDto.setResearcherLevel("Doctor of PhDs");
        userDto.setJobTitle("President of Space");
        userDto.setRoles(List.of(AccessRole.DATA_SUBMITTER.label));
        userDto.setStatus("active");

        return userDto;
    }

    @Test
    void updateUserInfo_HappyPath(){
        Integer userId = 1;
        UserDTO userDTO = getUserDto();
        User user = new User();
        user.setId(userId);
        Role role = new Role();
        role.setName(AccessRole.DATA_SUBMITTER.label);
        Institution institution = new Institution();
        institution.setName("Booz Allen Hamilton");
        LookupStatus status = new LookupStatus();
        status.setName("active");
        LookupResearcherLevel researcherLevel = new LookupResearcherLevel();
        researcherLevel.setName("Doctor of PhDs");
        LkupDCC dcc = new LkupDCC();
        dcc.setName("RADx Tech");

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));
        when(lookupRoleRepository.findAllByNameIn(anyList()))
                .thenReturn(List.of(role));
        when(institutionRepository.findByName(userDTO.getInstitution()))
                .thenReturn(Optional.of(institution));
        when(lookupStatusRepository.findLookupStatusByNameAndUsage(userDTO.getStatus(), "general"))
                .thenReturn(Optional.of(status));
        when(lookupResearcherLevelRepository.findByName(userDTO.getResearcherLevel()))
                .thenReturn(Optional.of(researcherLevel));
        when(dccRepository.findByName(userDTO.getDcc()))
                .thenReturn(Optional.of(dcc));

        UserDTO response = userService.updateUserInfo(userId, userDTO);

        assertEquals(1, response.getId());
        assertEquals("Booz Allen Hamilton", response.getInstitution());
        assertEquals("active", response.getStatus());
        assertEquals("Doctor of PhDs", response.getResearcherLevel());
        assertEquals("RADx Tech", response.getDcc());
        assertEquals("test@bah.com", response.getEmail());
        assertEquals("Test", response.getFirstName());
        assertEquals("McTestington", response.getLastName());
        assertEquals(List.of("Data Submitter"), response.getRoles());
    }

    @Test
    void updateUserInfo_NullDccNonSubmitter(){
        Integer userId = 1;
        UserDTO userDTO = getUserDto();
        userDTO.setDcc(null);
        userDTO.setRoles(List.of());
        User user = new User();
        user.setId(userId);
        Institution institution = new Institution();
        institution.setName("Booz Allen Hamilton");
        LookupStatus status = new LookupStatus();
        status.setName("active");
        LookupResearcherLevel researcherLevel = new LookupResearcherLevel();
        researcherLevel.setName("Doctor of PhDs");
        LkupDCC dcc = new LkupDCC();
        dcc.setName("RADx Tech");

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));
        when(institutionRepository.findByName(userDTO.getInstitution()))
                .thenReturn(Optional.of(institution));
        when(lookupStatusRepository.findLookupStatusByNameAndUsage(userDTO.getStatus(), "general"))
                .thenReturn(Optional.of(status));
        when(lookupResearcherLevelRepository.findByName(userDTO.getResearcherLevel()))
                .thenReturn(Optional.of(researcherLevel));

        UserDTO response = userService.updateUserInfo(userId, userDTO);

        verify(dccRepository, times(0))
                .findByName(anyString());

        assertEquals(1, response.getId());
        assertEquals("Booz Allen Hamilton", response.getInstitution());
        assertEquals("active", response.getStatus());
        assertEquals("Doctor of PhDs", response.getResearcherLevel());
        assertNull(response.getDcc());
        assertEquals("test@bah.com", response.getEmail());
        assertEquals("Test", response.getFirstName());
        assertEquals("McTestington", response.getLastName());
        assertEquals(List.of(), response.getRoles());
    }

    @Test
    void updateUserInfo_NullDccSubmitter(){
        Integer userId = 1;
        UserDTO userDTO = getUserDto();
        userDTO.setDcc(null);
        User user = new User();
        user.setId(userId);
        Role role = new Role();
        role.setName(AccessRole.DATA_SUBMITTER.label);
        Institution institution = new Institution();
        institution.setName("Booz Allen Hamilton");
        LookupStatus status = new LookupStatus();
        status.setName("active");
        LookupResearcherLevel researcherLevel = new LookupResearcherLevel();
        researcherLevel.setName("Doctor of PhDs");
        LkupDCC dcc = new LkupDCC();
        dcc.setName("RADx Tech");

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        assertThrows(SubmitterDccException.class, () -> userService.updateUserInfo(userId, userDTO));
    }

    @Test
    void updateUserInfo_UserNotFound(){
        Integer userId = 1;
        UserDTO userDTO = getUserDto();
        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.updateUserInfo(userId, userDTO));
    }

    @Test
    void updateUserInfo_SetSubmitterWithBlankDcc(){
        Integer userId = 1;
        UserDTO userDTO = getUserDto();
        userDTO.setDcc("");

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(new User()));

        assertThrows(SubmitterDccException.class, () -> userService.updateUserInfo(userId, userDTO));
    }

    @Test
    void updateUserInfo_InstituteNotFound(){
        Integer userId = 1;
        UserDTO userDTO = getUserDto();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(new User()));
        when(institutionRepository.findByName(userDTO.getInstitution()))
                .thenReturn(Optional.empty());

        assertThrows(UserInfoException.class, () -> userService.updateUserInfo(userId, userDTO));
    }

    @Test
    void updateUserInfo_StatusNotFound(){
        Integer userId = 1;
        UserDTO userDTO = getUserDto();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(new User()));
        when(institutionRepository.findByName(userDTO.getInstitution()))
                .thenReturn(Optional.of(new Institution()));
        when(lookupStatusRepository.findLookupStatusByNameAndUsage(userDTO.getStatus(), "general"))
                .thenReturn(Optional.empty());

        assertThrows(UserInfoException.class, () -> userService.updateUserInfo(userId, userDTO));
    }

    @Test
    void updateUserInfo_ResearcherLevelNotFound(){
        Integer userId = 1;
        UserDTO userDTO = getUserDto();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(new User()));
        when(institutionRepository.findByName(userDTO.getInstitution()))
                .thenReturn(Optional.of(new Institution()));
        when(lookupStatusRepository.findLookupStatusByNameAndUsage(userDTO.getStatus(), "general"))
                .thenReturn(Optional.of(new LookupStatus()));
        when(lookupResearcherLevelRepository.findByName(userDTO.getResearcherLevel()))
                .thenReturn(Optional.empty());

        assertThrows(UserInfoException.class, () -> userService.updateUserInfo(userId, userDTO));
    }

    @Test
    void updateUserInfo_DccNotFound(){
        Integer userId = 1;
        UserDTO userDTO = getUserDto();

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(new User()));
        when(institutionRepository.findByName(userDTO.getInstitution()))
                .thenReturn(Optional.of(new Institution()));
        when(lookupStatusRepository.findLookupStatusByNameAndUsage(userDTO.getStatus(), "general"))
                .thenReturn(Optional.of(new LookupStatus()));
        when(lookupResearcherLevelRepository.findByName(userDTO.getResearcherLevel()))
                .thenReturn(Optional.of(new LookupResearcherLevel()));
        when(dccRepository.findByName(userDTO.getDcc()))
                .thenReturn(Optional.empty());

        assertThrows(UserInfoException.class, () -> userService.updateUserInfo(userId, userDTO));
    }

    private UserRegistrationDTO getUserRegistrationDto(){
        UserRegistrationDTO dto = new UserRegistrationDTO();
        dto.setEmail("test@bah.com");
        dto.setFirstName("Test");
        dto.setLastName("McTestington");
        dto.setInstitution("Booz Allen Hamilton");
        dto.setResearcherLevel("Doctor of PhDs");
        dto.setJobTitle("President of Space");
        dto.setAcceptTerms(true);
        dto.setReferrers(new ArrayList<>());

        return dto;
    }

    @Test
    void saveUserRegistrationForm_HappyPath() {
        String sessionId = "session123";
        String email = "test@bah.com";
        AuthRasTracking rasTracking = new AuthRasTracking();
        rasTracking.setEmail(email);
        UserRegistrationDTO dto = getUserRegistrationDto();
        User user = new User();
        user.setId(1);
        user.setEmail(email);

        when(rasTrackingRepository.findRasTrackingBySessionId(sessionId))
                .thenReturn(Optional.of(rasTracking));
        when(userRepository.existsByEmail(email))
                .thenReturn(false);
        when(institutionRepository.findByName(dto.getInstitution()))
                .thenReturn(Optional.of(new Institution()));
        when(lookupStatusRepository.findByNameAndUsage("active", "general"))
                .thenReturn(Optional.of(new LookupStatus()));
        when(lookupResearcherLevelRepository.findByName(dto.getResearcherLevel()))
                .thenReturn(Optional.of(new LookupResearcherLevel()));
        when(userRepository.save(any(User.class)))
                .thenReturn(user);

        UserRegistrationDTO response = userService.saveUserRegistrationForm(sessionId, dto);

        assertEquals("test@bah.com", response.getEmail());
    }

    @Test
    void saveUserRegistrationForm_InvalidReferrer() {
        String sessionId = "session123";
        String email = "test@bah.com";
        AuthRasTracking rasTracking = new AuthRasTracking();
        rasTracking.setEmail(email);
        UserRegistrationDTO dto = getUserRegistrationDto();
        var referrer = new ReferrerSelectionDTO();
        referrer.setReferrerId(1);
        referrer.setReferrerSpecify("test");
        dto.getReferrers().add(referrer);
        User user = new User();
        user.setId(1);
        user.setEmail(email);

        when(rasTrackingRepository.findRasTrackingBySessionId(sessionId))
                .thenReturn(Optional.of(rasTracking));
        when(userRepository.existsByEmail(email))
                .thenReturn(false);
        when(institutionRepository.findByName(dto.getInstitution()))
                .thenReturn(Optional.of(new Institution()));
        when(lookupStatusRepository.findByNameAndUsage("active", "general"))
                .thenReturn(Optional.of(new LookupStatus()));
        when(lookupResearcherLevelRepository.findByName(dto.getResearcherLevel()))
                .thenReturn(Optional.of(new LookupResearcherLevel()));
        when(referrerRepository.findAll())
                .thenReturn(new ArrayList<>());

        assertThrows(UserRegistrationFormException.class, () -> userService.saveUserRegistrationForm(sessionId, dto));

    }

    @Test
    void saveUserRegistrationForm_InvalidSession() {
        String sessionId = "session123";
        when(rasTrackingRepository.findRasTrackingBySessionId(sessionId))
                .thenReturn(Optional.empty());
        assertThrows(UserAuthenticationException.class,
                     () -> userService.saveUserRegistrationForm(sessionId, new UserRegistrationDTO()));
    }

    @Test
    void saveUserRegistrationForm_RasTrackingNullEmail() {
        String sessionId = "session123";
        AuthRasTracking rasTracking = new AuthRasTracking();
        when(rasTrackingRepository.findRasTrackingBySessionId(sessionId))
                .thenReturn(Optional.of(rasTracking));

        assertThrows(BadDataException.class,
                     () -> userService.saveUserRegistrationForm(sessionId, new UserRegistrationDTO()));
    }

    @Test
    void saveUserRegistrationForm_PreexistingAccount() {
        String sessionId = "session123";
        String email = "test@bah.com";
        AuthRasTracking rasTracking = new AuthRasTracking();
        rasTracking.setEmail(email);
        when(rasTrackingRepository.findRasTrackingBySessionId(sessionId))
                .thenReturn(Optional.of(rasTracking));
        when(userRepository.existsByEmail(email))
                .thenReturn(true);
        assertThrows(UserRegistrationFormException.class,
                     () -> userService.saveUserRegistrationForm(sessionId, new UserRegistrationDTO()));
    }

    @Test
    void saveUserRegistrationForm_MissingRequiredFields() {
        String sessionId = "session123";
        String email = "test@bah.com";
        AuthRasTracking rasTracking = new AuthRasTracking();
        rasTracking.setEmail(email);
        UserRegistrationDTO dto = getUserRegistrationDto();
        dto.setEmail("");

        when(rasTrackingRepository.findRasTrackingBySessionId(sessionId))
                .thenReturn(Optional.of(rasTracking));
        when(userRepository.existsByEmail(email))
                .thenReturn(false);
        assertThrows(UserRegistrationFormException.class,
                     () -> userService.saveUserRegistrationForm(sessionId, dto));
    }

    @Test
    void saveUserRegistrationForm_InvalidInstitution() {
        String sessionId = "session123";
        String email = "test@bah.com";
        AuthRasTracking rasTracking = new AuthRasTracking();
        rasTracking.setEmail(email);
        UserRegistrationDTO dto = getUserRegistrationDto();

        when(rasTrackingRepository.findRasTrackingBySessionId(sessionId))
                .thenReturn(Optional.of(rasTracking));
        when(userRepository.existsByEmail(email))
                .thenReturn(false);
        when(institutionRepository.findByName(dto.getInstitution()))
                .thenReturn(Optional.empty());
        assertThrows(UserRegistrationFormException.class,
                     () -> userService.saveUserRegistrationForm(sessionId, dto));
    }

    @Test
    void saveUserRegistrationForm_InvalidResearcherLevel() {
        String sessionId = "session123";
        String email = "test@bah.com";
        AuthRasTracking rasTracking = new AuthRasTracking();
        rasTracking.setEmail(email);
        UserRegistrationDTO dto = getUserRegistrationDto();
        User user = new User();
        user.setId(1);
        user.setEmail(email);

        when(rasTrackingRepository.findRasTrackingBySessionId(sessionId))
                .thenReturn(Optional.of(rasTracking));
        when(userRepository.existsByEmail(email))
                .thenReturn(false);
        when(institutionRepository.findByName(dto.getInstitution()))
                .thenReturn(Optional.of(new Institution()));
        when(lookupStatusRepository.findByNameAndUsage("active", "general"))
                .thenReturn(Optional.of(new LookupStatus()));
        when(lookupResearcherLevelRepository.findByName(dto.getResearcherLevel()))
                .thenReturn(Optional.empty());

        assertThrows(UserRegistrationFormException.class,
                     () -> userService.saveUserRegistrationForm(sessionId, dto));
    }

    private InstitutionDTO getInstitutionDto(){
        InstitutionDTO dto = new InstitutionDTO();
        dto.setType("Testing Facility");
        dto.setName("Test LLC");
        dto.setIsForProfit(true);
        dto.setCountry("United States");
        dto.setState("Tennessee");
        return dto;
    }

    private Institution createInsitutionFromDto(InstitutionDTO dto) {
        Institution institution = new Institution();
        institution.setId(1);
        institution.setName(dto.getName());
        institution.setCreatedBy(9999);
        LookupState state = new LookupState();
        state.setName(dto.getState());
        institution.setState(state);
        LookupCountry country = new LookupCountry();
        country.setName(dto.getCountry());
        institution.setCountry(country);
        LookupStatus status = new LookupStatus();
        status.setName("pending");
        institution.setStatus(status);
        return institution;
    }

    @Test
    void createInstitution_HappyPath(){
        InstitutionDTO dto = getInstitutionDto();
        Institution institution = createInsitutionFromDto(dto);

        when(institutionRepository.existsByName(dto.getName()))
                .thenReturn(false);
        when(lookupInstitutionTypeRepository.findByName(dto.getType()))
                .thenReturn(Optional.of(new LookupInstitutionType()));
        when(lookupStatusRepository.findByNameAndUsage("pending", "institution"))
                .thenReturn(Optional.of(new LookupStatus()));
        when(lookupCountryRepository.findByName(dto.getCountry()))
                .thenReturn(Optional.of(new LookupCountry()));
        when(lookupStateRepository.findByName(dto.getState()))
                .thenReturn(Optional.of(new LookupState()));

        when(institutionRepository.save(any(Institution.class)))
                .thenReturn(institution);

        InstitutionDTO response = userService.createInstitution(dto);

        assertEquals(dto.getName(), response.getName());
        assertEquals(dto.getState(), response.getState());
        assertEquals(dto.getCountry(), response.getCountry());
        assertEquals(1, response.getId());
        assertEquals("pending", response.getStatus());
    }

    @Test
    void createInstitution_ExistingInstitution(){
        InstitutionDTO dto = getInstitutionDto();
        when(institutionRepository.existsByName(dto.getName()))
                .thenReturn(true);
        assertThrows(InstitutionCreationException.class, () -> userService.createInstitution(dto));
    }

    @Test
    void createInstitution_InvalidInstitutionType(){
        InstitutionDTO dto = getInstitutionDto();
        when(institutionRepository.existsByName(dto.getName()))
                .thenReturn(false);
        when(lookupInstitutionTypeRepository.findByName(dto.getType()))
                .thenReturn(Optional.empty());
        assertThrows(InstitutionCreationException.class, () -> userService.createInstitution(dto));
    }

    @Test
    void createInstitution_InvalidCountry(){
        InstitutionDTO dto = getInstitutionDto();
        when(institutionRepository.existsByName(dto.getName()))
                .thenReturn(false);
        when(lookupInstitutionTypeRepository.findByName(dto.getType()))
                .thenReturn(Optional.of(new LookupInstitutionType()));
        when(lookupStatusRepository.findByNameAndUsage("pending", "institution"))
                .thenReturn(Optional.of(new LookupStatus()));
        when(lookupCountryRepository.findByName(dto.getCountry()))
                .thenReturn(Optional.empty());

        assertThrows(InstitutionCreationException.class, () -> userService.createInstitution(dto));
    }

    @Test
    void createInstitution_InvalidState(){
        InstitutionDTO dto = getInstitutionDto();
        when(institutionRepository.existsByName(dto.getName()))
                .thenReturn(false);
        when(lookupInstitutionTypeRepository.findByName(dto.getType()))
                .thenReturn(Optional.of(new LookupInstitutionType()));
        when(lookupStatusRepository.findByNameAndUsage("pending", "institution"))
                .thenReturn(Optional.of(new LookupStatus()));
        when(lookupCountryRepository.findByName(dto.getCountry()))
                .thenReturn(Optional.of(new LookupCountry()));
        when(lookupStateRepository.findByName(dto.getState()))
                .thenReturn(Optional.empty());

        assertThrows(InstitutionCreationException.class, () -> userService.createInstitution(dto));
    }

    @Test
    void getUserInfoBySession_HappyPath(){
        String sessionId = "session123";
        String email = "test@bah.com";
        AuthRasTracking rasTracking = new AuthRasTracking();
        rasTracking.setEmail(email);
        User user = new User();
        user.setId(1);
        user.setEmail(email);
        Role role = new Role();
        role.setName(AccessRole.DATA_SUBMITTER.label);
        user.setRoles(List.of(role));

        when(rasTrackingRepository.findRasTrackingBySessionId(sessionId))
                .thenReturn(Optional.of(rasTracking));
        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        UserDTO response = userService.getUserInfoBySession(sessionId);

        assertEquals(1, response.getId());
        assertEquals("test@bah.com", response.getEmail());
        assertEquals(List.of("Data Submitter"), response.getRoles());
    }

    @Test
    void getUserInfoBySession_RasTrackingNotFound(){
        String sessionId = "session123";
        when(rasTrackingRepository.findRasTrackingBySessionId(sessionId))
                .thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.getUserInfoBySession(sessionId));
    }

    @Test
    void getUserInfoBySession_RasTrackingNullEmail(){
        String sessionId = "session123";
        when(rasTrackingRepository.findRasTrackingBySessionId(sessionId))
                .thenReturn(Optional.of(new AuthRasTracking()));
        assertThrows(BadDataException.class, () -> userService.getUserInfoBySession(sessionId));
    }

    @Test
    void getUserInfoBySession_UserNotFound(){
        String sessionId = "session123";
        String email = "test@bah.com";
        AuthRasTracking rasTracking = new AuthRasTracking();
        rasTracking.setEmail(email);

        when(rasTrackingRepository.findRasTrackingBySessionId(sessionId))
                .thenReturn(Optional.of(rasTracking));
        when(userRepository.findByEmail(email))
                .thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.getUserInfoBySession(sessionId));
    }

    @Test
    void getUserInfoById_HappyPath(){
        Integer id = 1;
        User user = new User();
        user.setId(id);
        Role role = new Role();
        role.setName(AccessRole.DATA_SUBMITTER.label);
        user.setRoles(List.of(role));

        when(userRepository.findById(id))
                .thenReturn(Optional.of(user));

        UserDTO response = userService.getUserInfoById(id);

        assertEquals(1, response.getId());
        assertEquals(List.of("Data Submitter"), response.getRoles());
    }

    @Test
    void getUserInfoById_UserNotFound(){
        Integer id = 1;
        when(userRepository.findById(id))
                .thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.getUserInfoById(id));
    }

    @Test
    void getUserInfo_HappyPath(){
        String email = "test@bah.com";
        User user = new User();
        user.setId(1);
        user.setEmail(email);
        Role role = new Role();
        role.setName(AccessRole.DATA_SUBMITTER.label);
        user.setRoles(List.of(role));

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        UserDTO response = userService.getUserInfo(email);

        assertEquals(1, response.getId());
        assertEquals("test@bah.com", response.getEmail());
        assertEquals(List.of("Data Submitter"), response.getRoles());
    }

    @Test
    void getUserInfo_UserNotFound(){
        String email = "test@bah.com";
        when(userRepository.findByEmail(email))
                .thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.getUserInfo(email));
    }
}
