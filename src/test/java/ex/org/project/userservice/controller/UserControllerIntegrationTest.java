package ex.org.project.userservice.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testGetCurrentUserInfo_WithValidJWT_ReturnsUserFromDatabase() throws Exception {
        // Create a mock JWT with an email that exists in your database
        // IMPORTANT: Replace "test@example.com" with an actual email from your auth_user table
        String testEmail = "test.user.1@datahub.orgx";
        
        Jwt jwt = Jwt.withTokenValue("mock-token")
                .header("alg", "RS256")
                .claim("sub", "mock-subject-123")
                .claim("email", testEmail)
                .claim("preferred_username", testEmail)
                .claim("name", "Test 1 User")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        // Call the endpoint with the mocked JWT
        mockMvc.perform(get("/user/info")
                .with(jwt().jwt(jwt)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(testEmail))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.firstName").exists());
    }

    @Test
    public void testGetCurrentUserInfo_WithNonExistentUser_Returns404() throws Exception {
        // Email that doesn't exist in database
        String nonExistentEmail = "nonexistent@example.com";
        
        Jwt jwt = Jwt.withTokenValue("mock-token")
                .header("alg", "RS256")
                .claim("sub", "mock-subject-456")
                .claim("email", nonExistentEmail)
                .claim("preferred_username", nonExistentEmail)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        mockMvc.perform(get("/user/info")
                .with(jwt().jwt(jwt)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetCurrentUserInfo_WithoutJWT_Returns401() throws Exception {
        mockMvc.perform(get("/user/info"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }
}

