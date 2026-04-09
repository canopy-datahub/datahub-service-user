package ex.org.project.userservice.service;

import ex.org.project.userservice.dto.SupportAssigneeDTO;
import ex.org.project.userservice.dto.SupportRequestDTO;

import java.io.IOException;
import java.util.List;

public interface SupportRequestService {

	List<String> getSupportRequestTypeNames();

    SupportRequestDTO saveSupportRequest(SupportRequestDTO supportRequestDTO);

    SupportRequestDTO getSupportRequestById(Integer id, Boolean isStaff);

    List<SupportRequestDTO> getAllSupportRequestsByStatus(String status);

    List<SupportRequestDTO> getAllSupportRequests();

    SupportRequestDTO updateSupportRequest(Integer userId, Integer id, SupportRequestDTO supportRequestDTO);

    List<Integer> getAllSeverities();

    List<String> getAllResolutionTypes();

    List<String> getAllStatuses();

    List<SupportAssigneeDTO> getAllAssignees();

    String downloadSupportRequestReportsToCSV() throws IOException;

    SupportRequestDTO saveLoggedInUserSupportRequest(SupportRequestDTO supportRequestDTO, Integer userId);
}
