package ex.org.project.userservice.controller;

import java.util.List;

import ex.org.project.userservice.auth.AccessRole;
import ex.org.project.userservice.auth.UserAuthService;
import ex.org.project.userservice.dto.*;
import ex.org.project.userservice.entity.LkupCenter;
import ex.org.project.userservice.entity.Role;
import ex.org.project.userservice.util.RequestValidator;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ex.org.project.userservice.service.UserService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final UserAuthService authService;


    @GetMapping("/admin/user")
    public ResponseEntity<UserDTO> getUserInfo(@CookieValue(value="chocolateChip", required = false) String sessionId,
                                               @RequestParam String emailAddress) {
        authService.checkAuth(sessionId, List.of(AccessRole.ADMIN));
        UserDTO userDTO = userService.getUserInfo(emailAddress);
        return ResponseEntity.ok(userDTO);
    }

    @GetMapping("/info")
    public ResponseEntity<UserDTO> getUserInfoBySessionCookie(@CookieValue(value="chocolateChip", required = false) String sessionId) {
        authService.checkAuth(sessionId);
        UserDTO userDTO = userService.getUserInfoBySession(sessionId);
        return ResponseEntity.ok(userDTO);
    }

    @GetMapping("/infoBySession")
    public ResponseEntity<UserDTO> getUserInfoBySessionParam(@RequestParam(required = false) String sessionId) {
        authService.checkAuth(sessionId);
        UserDTO userDTO = userService.getUserInfoBySession(sessionId);
        return ResponseEntity.ok(userDTO);
    }

    @GetMapping("/approved-institutions")
    public ResponseEntity<List<InstitutionDTO>> getAllApprovedInstitutions() {
        List<InstitutionDTO> approvedInstitutions = userService.getApprovedInstitutions();
        return ResponseEntity.ok(approvedInstitutions);
    }

    @PostMapping("/create-institution")
    public ResponseEntity<InstitutionDTO> createInstitution(@RequestBody InstitutionDTO institutionDTO) {
        InstitutionDTO createdInstitution = userService.createInstitution(institutionDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdInstitution);
    }

    @PostMapping("/user-registration")
    public ResponseEntity<UserRegistrationDTO> saveUserRegistrationForm(@RequestParam(required = false) String sessionId,
                                                                        @RequestBody @Valid UserRegistrationDTO userRegistrationDTO) {
        RequestValidator.validateStringRequestParam(sessionId);
        UserRegistrationDTO savedRequest = userService.saveUserRegistrationForm(sessionId, userRegistrationDTO);
        return ResponseEntity.ok(savedRequest);
    }

    @GetMapping("/referrer-types")
    public ResponseEntity<List<LkupReferrerDTO>> getReferrerTypes() {
        return ResponseEntity.ok(userService.getReferrerTypes());
    }

    @GetMapping("/researcher-levels")
    public ResponseEntity<List<String>> getAllResearcherLevels() {
        List<String> researcherLevels = userService.getResearcherLevels();
        return ResponseEntity.ok(researcherLevels);
    }

    @GetMapping("/institution-types")
    public ResponseEntity<List<String>> getAllInstitutionTypes() {
        List<String> institutionTypes = userService.getAllInstitutionTypes();
        return ResponseEntity.ok(institutionTypes);
    }

    @GetMapping("/states")
    public ResponseEntity<List<LookupStateDTO>> getAllStates() {
        List<LookupStateDTO> states = userService.getAllStates();
        return ResponseEntity.ok(states);
    }

    @GetMapping("/countries")
    public ResponseEntity<List<LookupCountryDTO>> getAllCountries() {
        List<LookupCountryDTO> countries = userService.getAllCountries();
        return ResponseEntity.ok(countries);
    }

    @GetMapping("/centers")
    public ResponseEntity<List<LkupCenter>> getAllCenters() {
        List<LkupCenter> dccs = userService.getAllCenters();
        return ResponseEntity.ok(dccs);
    }

    @GetMapping("/admin/users")
    public ResponseEntity<List<UserDTO>> getUsersByStatus(@CookieValue(value="chocolateChip", required = false) String sessionId,
                                                          @RequestParam(required = true) String status) {
        authService.checkAuth(sessionId, List.of(AccessRole.ADMIN));
        List<UserDTO> users = userService.getUsersByStatus(status);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/admin/roles")
    public ResponseEntity<List<Role>> getAllRoles(@CookieValue(value="chocolateChip", required = false) String sessionId) {
        authService.checkAuth(sessionId, List.of(AccessRole.ADMIN));
        List<Role> userRoles = userService.getAllRoles();
        return ResponseEntity.ok(userRoles);
    }

    @GetMapping("/admin/general-statuses")
    public ResponseEntity<List<String>> getGeneralStatus(@CookieValue(value="chocolateChip", required = false) String sessionId) {
        authService.checkAuth(sessionId, List.of(AccessRole.ADMIN));
        List<String> generalStatuses = userService.getGeneralStatus();
        return ResponseEntity.ok(generalStatuses);
    }

    @GetMapping("/admin/{id}")
    public ResponseEntity<UserDTO> getUserInfoById(@CookieValue(value="chocolateChip", required = false) String sessionId,
                                                   @PathVariable Integer id) {
        authService.checkAuth(sessionId, List.of(AccessRole.ADMIN));
        return ResponseEntity.ok(userService.getUserInfoById(id));
    }

    @PutMapping("/admin/update/{id}")
    public ResponseEntity<UserDTO> updateUserInfo(@CookieValue(value="chocolateChip", required = false) String sessionId,
                                                  @PathVariable Integer id, @RequestBody UserDTO userDTO) {
        authService.checkAuth(sessionId, List.of(AccessRole.ADMIN));
        UserDTO updatedUserInfo = userService.updateUserInfo(id, userDTO);
        return ResponseEntity.ok(updatedUserInfo);
    }

    @PutMapping("/editProfile")
    public ResponseEntity<UserDTO> editProfile(@CookieValue(value="chocolateChip", required = false) String sessionId,
                                                  @RequestBody UserDTO userDTO) {
        Integer id = authService.checkAuth(sessionId);
        UserDTO updatedUserInfo = userService.editProfile(id, userDTO);
        return ResponseEntity.ok(updatedUserInfo);
    }

    @GetMapping("/profile")
    public ResponseEntity<UserDTO> getUserProfile(@CookieValue(value="chocolateChip", required = false) String sessionId) {
        Integer userId = authService.checkAuth(sessionId);
        return ResponseEntity.ok(userService.getUserInfoById(userId));
    }
}
