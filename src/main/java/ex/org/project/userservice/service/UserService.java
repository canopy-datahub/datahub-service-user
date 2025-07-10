package ex.org.project.userservice.service;

import java.util.List;

import ex.org.project.userservice.dto.*;
import ex.org.project.userservice.entity.LkupDCC;
import ex.org.project.userservice.entity.Role;

public interface UserService {

    UserDTO getUserInfo(String emailAddress);
    
    UserDTO getUserInfoBySession(String session);

    List<InstitutionDTO> getApprovedInstitutions();

    InstitutionDTO createInstitution(InstitutionDTO institutionDTO);

    UserRegistrationDTO saveUserRegistrationForm(String sessionId, UserRegistrationDTO userRegistrationDTO);

    List<String> getResearcherLevels();

    List<String> getAllInstitutionTypes();

    List<LookupStateDTO> getAllStates();

    List<LookupCountryDTO> getAllCountries();
    
    List<LkupDCC> getAllDCCs();

    List<UserDTO> getUsersByStatus(String status);

    List<Role> getAllRoles();

    List<String> getGeneralStatus();

    UserDTO getUserInfoById(Integer id);

    UserDTO updateUserInfo(Integer id, UserDTO userDTO);

    UserDTO editProfile(Integer id, UserDTO userDTO);

    List<LkupReferrerDTO> getReferrerTypes();

}
