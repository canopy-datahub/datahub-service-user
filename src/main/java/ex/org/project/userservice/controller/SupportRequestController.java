package ex.org.project.userservice.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import ex.org.project.userservice.auth.AccessRole;
import ex.org.project.userservice.auth.UserAuthenticationException;

import ex.org.project.userservice.auth.core.KeycloakAuthenticationService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ex.org.project.userservice.dto.SupportAssigneeDTO;
import ex.org.project.userservice.dto.SupportRequestDTO;
import ex.org.project.userservice.service.SupportRequestService;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;


@RestController
@RequiredArgsConstructor
@RequestMapping("/support-request")
public class SupportRequestController {

    private final SupportRequestService supportRequestService;
    private final KeycloakAuthenticationService authenticationService;

    @GetMapping("/request-types")
    public ResponseEntity<List<String>> getLookupSupportRequestType() {
        List<String> requestTypeNames = supportRequestService.getSupportRequestTypeNames();
        return ResponseEntity.ok(requestTypeNames);
    }

    @PostMapping("/submit")
    public ResponseEntity<SupportRequestDTO> saveSupportRequest(@AuthenticationPrincipal Jwt jwt,
                                                                @RequestBody SupportRequestDTO supportRequestDTO) {
        SupportRequestDTO savedRequest;
        try{
             Integer userId = authenticationService.checkAuth(jwt);
            savedRequest = supportRequestService.saveLoggedInUserSupportRequest(supportRequestDTO, userId);
        }catch(UserAuthenticationException ignored)
        {
            savedRequest = supportRequestService.saveSupportRequest(supportRequestDTO);
        }
        return ResponseEntity.ok(savedRequest);
    }

    @GetMapping("/all-support-requests")
    public ResponseEntity<List<SupportRequestDTO>> getAllSupportRequests(@AuthenticationPrincipal Jwt jwt,
                                                                         @RequestParam(required = false) String status) {
        authenticationService.checkAuth(jwt, List.of(AccessRole.SUPPORT_TEAM, AccessRole.ADMIN, AccessRole.OFFICER));
        if (status == null || status.equalsIgnoreCase("All")) {
            List<SupportRequestDTO> fetchedSupportRequests = supportRequestService.getAllSupportRequests();
            return ResponseEntity.ok(fetchedSupportRequests);
        } else {
            List<SupportRequestDTO> supportRequests = supportRequestService.getAllSupportRequestsByStatus(status);
            return ResponseEntity.ok(supportRequests);
        }
    }

	@GetMapping("/{id}")
	public ResponseEntity<SupportRequestDTO> getSupportRequestById(@AuthenticationPrincipal Jwt jwt,
                                                                   @PathVariable Integer id) {
        authenticationService.checkAuth(jwt, List.of(AccessRole.SUPPORT_TEAM, AccessRole.ADMIN, AccessRole.OFFICER));
        return ResponseEntity.ok(supportRequestService.getSupportRequestById(id, true));
	}

	@GetMapping("/officer/{id}")
	public ResponseEntity<SupportRequestDTO> getSupportRequestForOfficerById(@AuthenticationPrincipal Jwt jwt,
                                                                             @PathVariable Integer id) {
        authenticationService.checkAuth(jwt, List.of(AccessRole.OFFICER));
        return ResponseEntity.ok(supportRequestService.getSupportRequestById(id, false));
	}


    @PutMapping("/update-support-request/{id}")
    public ResponseEntity<SupportRequestDTO> updateSupportRequest(@AuthenticationPrincipal Jwt jwt,
                                                  @PathVariable Integer id,
                                                  @RequestBody SupportRequestDTO supportRequestDTO) {
        Integer userId = authenticationService.checkAuth(jwt, List.of(AccessRole.SUPPORT_TEAM, AccessRole.ADMIN));
        SupportRequestDTO updatedSupportRequest = supportRequestService.updateSupportRequest(userId, id, supportRequestDTO);
        return ResponseEntity.ok(updatedSupportRequest);
    }

    @GetMapping("/all-statuses")
    public ResponseEntity<List<String>> getAllValidStatuses(@AuthenticationPrincipal Jwt jwt) {
      authenticationService.checkAuth(jwt, List.of(AccessRole.SUPPORT_TEAM, AccessRole.ADMIN, AccessRole.OFFICER));
        List<String> statuses = supportRequestService.getAllStatuses();
        return ResponseEntity.ok(statuses);
    }

    @GetMapping("/all-severity")
    public ResponseEntity<List<Integer>> getAllValidSeverities(@AuthenticationPrincipal Jwt jwt) {
        authenticationService.checkAuth(jwt, List.of(AccessRole.SUPPORT_TEAM, AccessRole.ADMIN, AccessRole.OFFICER));
        List<Integer> severities = supportRequestService.getAllSeverities();
        return ResponseEntity.ok(severities);
    }

    @GetMapping("/all-resolution-types")
    public ResponseEntity<List<String>> getAllValidResolutionTypes(@AuthenticationPrincipal Jwt jwt) {
        authenticationService.checkAuth(jwt, List.of(AccessRole.SUPPORT_TEAM, AccessRole.ADMIN, AccessRole.OFFICER));
        List<String> resolutionTypes = supportRequestService.getAllResolutionTypes();
        return ResponseEntity.ok(resolutionTypes);
    }

    @GetMapping("/all-assignees")
    public ResponseEntity<List<SupportAssigneeDTO>> getAllValidAssignees(@AuthenticationPrincipal Jwt jwt) {
        authenticationService.checkAuth(jwt, List.of(AccessRole.SUPPORT_TEAM, AccessRole.ADMIN, AccessRole.OFFICER));
        List<SupportAssigneeDTO> assignees = supportRequestService.getAllAssignees();
        return ResponseEntity.ok(assignees);
    }

    @GetMapping("/download-support-request-report")
    public ResponseEntity<byte[]> downloadSupportRequestReports(@AuthenticationPrincipal Jwt jwt) throws IOException {
        authenticationService.checkAuth(jwt, List.of(AccessRole.SUPPORT_TEAM, AccessRole.ADMIN, AccessRole.OFFICER));
        String path = supportRequestService.downloadSupportRequestReportsToCSV();

        // Read the CSV file and convert it to a byte array
        byte[] csvBytes = Files.readAllBytes(Paths.get(path));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "support_request.csv");

        return ResponseEntity.ok().headers(headers).body(csvBytes);
    }
}
