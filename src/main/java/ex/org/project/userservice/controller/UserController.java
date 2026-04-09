package ex.org.project.userservice.controller;

import java.util.List;

import ex.org.project.userservice.auth.core.KeycloakAuthenticationService;
import ex.org.project.userservice.auth.AccessRole;
import ex.org.project.userservice.dto.*;
import ex.org.project.userservice.entity.LkupCenter;
import ex.org.project.userservice.entity.Role;
import ex.org.project.userservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final KeycloakAuthenticationService authenticationService;


  @GetMapping("/admin/user")
    public ResponseEntity<UserDTO> getUserInfo(@AuthenticationPrincipal Jwt jwt,
                                               @RequestParam String emailAddress) {
    authenticationService.checkAuth(jwt, List.of(AccessRole.ADMIN));
        UserDTO userDTO = userService.getUserInfo(emailAddress);
        return ResponseEntity.ok(userDTO);
    }

    @GetMapping("/info")
    public ResponseEntity<UserDTO> getCurrentUserInfo(@AuthenticationPrincipal Jwt jwt) {
      System.out.println("UserController.getCurrentUserInfo:");
      System.out.println(jwt);
        Integer userId = authenticationService.checkAuth(jwt);
      System.out.println(userId);
        UserDTO userDTO = userService.getUserInfoById(userId);
      System.out.println(userDTO);
      if (userDTO != null) {
        System.out.println(userDTO.getEmail());
      }
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
    public ResponseEntity<UserRegistrationDTO> saveUserRegistrationForm(@AuthenticationPrincipal Jwt jwt,
                                                                        @RequestBody @Valid UserRegistrationDTO userRegistrationDTO) {
      // User registration can be optionally authenticated
      Integer userId = null;
      if (jwt != null) {
        try {
          userId = authenticationService.getAuthenticatedUserId(jwt);
        } catch (Exception e) {
          // If JWT is invalid or user not found, proceed with anonymous registration
        }
      }
      UserRegistrationDTO savedRequest = userService.saveUserRegistrationForm(userId, userRegistrationDTO);
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
    public ResponseEntity<List<UserDTO>> getUsersByStatus(@AuthenticationPrincipal Jwt jwt,
                                                          @RequestParam(required = true) String status) {
      authenticationService.checkAuth(jwt, List.of(AccessRole.ADMIN));
        List<UserDTO> users = userService.getUsersByStatus(status);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/admin/roles")
    public ResponseEntity<List<Role>> getAllRoles(@AuthenticationPrincipal Jwt jwt) {
      authenticationService.checkAuth(jwt, List.of(AccessRole.ADMIN));
        List<Role> userRoles = userService.getAllRoles();
        return ResponseEntity.ok(userRoles);
    }

    @GetMapping("/admin/general-statuses")
    public ResponseEntity<List<String>> getGeneralStatus(@AuthenticationPrincipal Jwt jwt) {
      authenticationService.checkAuth(jwt, List.of(AccessRole.ADMIN));
        List<String> generalStatuses = userService.getGeneralStatus();
        return ResponseEntity.ok(generalStatuses);
    }

    @GetMapping("/admin/{id}")
    public ResponseEntity<UserDTO> getUserInfoById(@AuthenticationPrincipal Jwt jwt,
                                                   @PathVariable Integer id) {
      authenticationService.checkAuth(jwt, List.of(AccessRole.ADMIN));
        return ResponseEntity.ok(userService.getUserInfoById(id));
    }

    @PutMapping("/admin/update/{id}")
    public ResponseEntity<UserDTO> updateUserInfo(@AuthenticationPrincipal Jwt jwt,
                                                  @PathVariable Integer id, @RequestBody UserDTO userDTO) {
      authenticationService.checkAuth(jwt, List.of(AccessRole.ADMIN));
        UserDTO updatedUserInfo = userService.updateUserInfo(id, userDTO);
        return ResponseEntity.ok(updatedUserInfo);
    }

    @PutMapping("/editProfile")
    public ResponseEntity<UserDTO> editProfile(@AuthenticationPrincipal Jwt jwt,
                                                  @RequestBody UserDTO userDTO) {
        Integer id = authenticationService.checkAuth(jwt);
        UserDTO updatedUserInfo = userService.editProfile(id, userDTO);
        return ResponseEntity.ok(updatedUserInfo);
    }

    @GetMapping("/profile")
    public ResponseEntity<UserDTO> getUserProfile(@AuthenticationPrincipal Jwt jwt) {
        Integer userId = authenticationService.checkAuth(jwt);
        return ResponseEntity.ok(userService.getUserInfoById(userId));
    }
}
