package ex.org.project.userservice.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import ex.org.project.userservice.auth.AccessRole;
import ex.org.project.userservice.auth.UserAuthService;
import ex.org.project.userservice.auth.UserAuthenticationException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ex.org.project.userservice.dto.SupportAssigneeDTO;
import ex.org.project.userservice.dto.SupportRequestDTO;
import ex.org.project.userservice.service.SupportRequestService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/support-request")
public class SupportRequestController {

    private final SupportRequestService supportRequestService;
    private final UserAuthService authService;

    @GetMapping("/request-types")
    public ResponseEntity<List<String>> getLookupSupportRequestType() {
        List<String> requestTypeNames = supportRequestService.getSupportRequestTypeNames();
        return ResponseEntity.ok(requestTypeNames);
    }

    @PostMapping("/submit")
    public ResponseEntity<SupportRequestDTO> saveSupportRequest(@CookieValue(value = "chocolateChip", required = false) String sessionId,
                                                                @RequestBody SupportRequestDTO supportRequestDTO) {
        SupportRequestDTO savedRequest;
        try{
             Integer userId = authService.checkAuth(sessionId);
            savedRequest = supportRequestService.saveLoggedInUserSupportRequest(supportRequestDTO, userId);
        }catch(UserAuthenticationException ignored)
        {
            savedRequest = supportRequestService.saveSupportRequest(supportRequestDTO);
        }
        return ResponseEntity.ok(savedRequest);
    }

    @GetMapping("/all-support-requests")
    public ResponseEntity<List<SupportRequestDTO>> getAllSupportRequests(@CookieValue(value = "chocolateChip", required = false) String sessionId,
                                                                         @RequestParam(required = false) String status) {
        authService.checkAuth(sessionId, List.of(AccessRole.SUPPORT_TEAM, AccessRole.ADMIN, AccessRole.OFFICER));
        if (status == null || status.equalsIgnoreCase("All")) {
            List<SupportRequestDTO> fetchedSupportRequests = supportRequestService.getAllSupportRequests();
            return ResponseEntity.ok(fetchedSupportRequests);
        } else {
            List<SupportRequestDTO> supportRequests = supportRequestService.getAllSupportRequestsByStatus(status);
            return ResponseEntity.ok(supportRequests);
        }
    }

	@GetMapping("/{id}")
	public ResponseEntity<SupportRequestDTO> getSupportRequestById(@CookieValue(value = "chocolateChip", required = false) String sessionId,
                                                                   @PathVariable Integer id) {
        authService.checkAuth(sessionId, List.of(AccessRole.SUPPORT_TEAM, AccessRole.ADMIN, AccessRole.OFFICER));
        return ResponseEntity.ok(supportRequestService.getSupportRequestById(id, true));
	}

	@GetMapping("/officer/{id}")
	public ResponseEntity<SupportRequestDTO> getSupportRequestForOfficerById(@CookieValue(value = "chocolateChip", required = false) String sessionId,
                                                                             @PathVariable Integer id) {
        authService.checkAuth(sessionId, List.of(AccessRole.OFFICER));
        return ResponseEntity.ok(supportRequestService.getSupportRequestById(id, false));
	}


    @PutMapping("/update-support-request/{id}")
    public ResponseEntity<SupportRequestDTO> updateSupportRequest(@CookieValue(value = "chocolateChip", required = false) String sessionId,
                                                  @PathVariable Integer id,
                                                  @RequestBody SupportRequestDTO supportRequestDTO) {
        Integer userId = authService.checkAuth(sessionId, List.of(AccessRole.SUPPORT_TEAM, AccessRole.ADMIN));
        SupportRequestDTO updatedSupportRequest = supportRequestService.updateSupportRequest(userId, id, supportRequestDTO);
        return ResponseEntity.ok(updatedSupportRequest);
    }

    @GetMapping("/all-statuses")
    public ResponseEntity<List<String>> getAllValidStatuses(@CookieValue(value = "chocolateChip", required = false) String sessionId) {
        authService.checkAuth(sessionId, List.of(AccessRole.SUPPORT_TEAM, AccessRole.ADMIN, AccessRole.OFFICER));
        List<String> statuses = supportRequestService.getAllStatuses();
        return ResponseEntity.ok(statuses);
    }

    @GetMapping("/all-severity")
    public ResponseEntity<List<Integer>> getAllValidSeverities(@CookieValue(value = "chocolateChip", required = false) String sessionId) {
        authService.checkAuth(sessionId, List.of(AccessRole.SUPPORT_TEAM, AccessRole.ADMIN, AccessRole.OFFICER));
        List<Integer> severities = supportRequestService.getAllSeverities();
        return ResponseEntity.ok(severities);
    }

    @GetMapping("/all-resolution-types")
    public ResponseEntity<List<String>> getAllValidResolutionTypes(@CookieValue(value = "chocolateChip", required = false) String sessionId) {
        authService.checkAuth(sessionId, List.of(AccessRole.SUPPORT_TEAM, AccessRole.ADMIN, AccessRole.OFFICER));
        List<String> resolutionTypes = supportRequestService.getAllResolutionTypes();
        return ResponseEntity.ok(resolutionTypes);
    }

    @GetMapping("/all-assignees")
    public ResponseEntity<List<SupportAssigneeDTO>> getAllValidAssignees(@CookieValue(value = "chocolateChip", required = false) String sessionId) {
        authService.checkAuth(sessionId, List.of(AccessRole.SUPPORT_TEAM, AccessRole.ADMIN, AccessRole.OFFICER));
        List<SupportAssigneeDTO> assignees = supportRequestService.getAllAssignees();
        return ResponseEntity.ok(assignees);
    }

    @GetMapping("/download-support-request-report")
    public ResponseEntity<byte[]> downloadSupportRequestReports(@RequestParam(required = false) String sessionId) throws IOException {
        authService.checkAuth(sessionId, List.of(AccessRole.SUPPORT_TEAM, AccessRole.ADMIN, AccessRole.OFFICER));
        String path = supportRequestService.downloadSupportRequestReportsToCSV();

        // Read the CSV file and convert it to a byte array
        byte[] csvBytes = Files.readAllBytes(Paths.get(path));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "support_request.csv");

        return ResponseEntity.ok().headers(headers).body(csvBytes);
    }
}