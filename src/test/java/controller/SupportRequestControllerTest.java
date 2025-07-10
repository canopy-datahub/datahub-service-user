package controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.List;

import ex.org.project.userservice.auth.UserAuthService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import ex.org.project.userservice.controller.SupportRequestController;
import ex.org.project.userservice.dto.SupportRequestDTO;
import ex.org.project.userservice.service.SupportRequestService;

public class SupportRequestControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SupportRequestService supportRequestService;

    @Mock
    private UserAuthService authService;

    @InjectMocks
    private SupportRequestController supportRequestController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(supportRequestController).build();
    }

    @Test
    public void testGetLookupSupportRequestType() throws Exception {
        List<String> requestTypes = Collections.singletonList("Feature Request");
        when(supportRequestService.getSupportRequestTypeNames()).thenReturn(requestTypes);

        mockMvc.perform(get("/support-request/request-types"))
                .andExpect(status().isOk())
                .andExpect(content().json(new ObjectMapper().writeValueAsString(requestTypes)));

        verify(supportRequestService, times(1)).getSupportRequestTypeNames();
    }

    @Test
    public void testSaveSupportRequest() throws Exception {
        SupportRequestDTO requestDTO = new SupportRequestDTO();
        requestDTO.setFullName("Johnny Nashville");
        requestDTO.setEmail("johnny.nashville@gmail.com");
        requestDTO.setRequestTitle("Test Request");
        requestDTO.setRequestType("Technical");
        requestDTO.setRequestDetail("Test Request Detail");

        when(supportRequestService.saveLoggedInUserSupportRequest(any(), any())).thenReturn(requestDTO);

        mockMvc.perform(post("/support-request/submit")
                        .cookie(new Cookie("chocolateChip", "session123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestTitle").value(requestDTO.getRequestTitle()));

        verify(supportRequestService, times(1)).saveLoggedInUserSupportRequest(any(), any());
    }

    @Test
    public void testGetAllSupportRequests() throws Exception {
        SupportRequestDTO supportRequest = new SupportRequestDTO();
        supportRequest.setRequestTitle("Test");
        List<SupportRequestDTO> supportRequests = Collections.singletonList(supportRequest);

        when(supportRequestService.getAllSupportRequests()).thenReturn(supportRequests);

        mockMvc.perform(get("/support-request/all-support-requests")
                        .cookie(new Cookie("chocolateChip", "session123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].requestTitle").value(supportRequest.getRequestTitle()));

        verify(supportRequestService, times(1)).getAllSupportRequests();
    }

}
