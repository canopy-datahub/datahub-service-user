package ex.org.project.userservice.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

@ExtendWith(MockitoExtension.class)
class UserAuthenticationServiceTest {

    @Mock
    private AuthUserRepository authUserRepository;

    @Mock
    private KeycloakJwtService keycloakJwtService;

    @InjectMocks
    private UserAuthenticationService authenticationService;

    private Jwt mockJwt;
    private AuthUser mockUser;
    private AuthRole mockRole;

    @BeforeEach
    void setUp() {
        // Create mock JWT
        Map<String, Object> headers = new HashMap<>();
        headers.put("alg", "RS256");
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "test-user-id");
        claims.put("email", "test@example.com");
        claims.put("preferred_username", "testuser");
        
        mockJwt = new Jwt(
            "test-token",
            Instant.now(),
            Instant.now().plusSeconds(3600),
            headers,
            claims
        );

        // Create mock user with role
        mockRole = new AuthRole();
        mockRole.setId(1);
        mockRole.setName("Data Submitter");

        mockUser = new AuthUser();
        mockUser.setId(1);
        mockUser.setEmail("test@example.com");
        mockUser.setRoles(Collections.singletonList(mockRole));
    }

    @Test
    void testGetAuthenticatedUser_Success() {
        // Given
        when(keycloakJwtService.extractEmail(mockJwt)).thenReturn("test@example.com");
        when(authUserRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));

        // When
        AuthUser result = authenticationService.getAuthenticatedUser(mockJwt);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("test@example.com", result.getEmail());
        verify(keycloakJwtService, times(1)).extractEmail(mockJwt);
        verify(authUserRepository, times(1)).findByEmail("test@example.com");
    }

    @Test
    void testGetAuthenticatedUser_UserNotFound() {
        // Given
        when(keycloakJwtService.extractEmail(mockJwt)).thenReturn("notfound@example.com");
        when(authUserRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UserNotFoundException.class, 
            () -> authenticationService.getAuthenticatedUser(mockJwt));
    }

    @Test
    void testGetAuthenticatedUserId_Success() {
        // Given
        when(keycloakJwtService.extractEmail(mockJwt)).thenReturn("test@example.com");
        when(authUserRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));

        // When
        Integer userId = authenticationService.getAuthenticatedUserId(mockJwt);

        // Then
        assertNotNull(userId);
        assertEquals(1, userId);
    }

    @Test
    void testHasAnyRole_WithMatchingRole() {
        // Given
        List<AccessRole> requiredRoles = Collections.singletonList(AccessRole.DATA_SUBMITTER);

        // When
        boolean result = authenticationService.hasAnyRole(mockUser, requiredRoles);

        // Then
        assertTrue(result);
    }

    @Test
    void testHasAnyRole_WithoutMatchingRole() {
        // Given
        List<AccessRole> requiredRoles = Collections.singletonList(AccessRole.ADMIN);

        // When
        boolean result = authenticationService.hasAnyRole(mockUser, requiredRoles);

        // Then
        assertFalse(result);
    }

    @Test
    void testHasAnyRole_EmptyRequiredRoles() {
        // Given
        List<AccessRole> requiredRoles = Collections.emptyList();

        // When
        boolean result = authenticationService.hasAnyRole(mockUser, requiredRoles);

        // Then
        assertTrue(result);
    }

    @Test
    void testCheckAuth_WithValidRole() {
        // Given
        List<AccessRole> requiredRoles = Collections.singletonList(AccessRole.DATA_SUBMITTER);
        when(keycloakJwtService.extractEmail(mockJwt)).thenReturn("test@example.com");
        when(authUserRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));

        // When
        Integer userId = authenticationService.checkAuth(mockJwt, requiredRoles);

        // Then
        assertNotNull(userId);
        assertEquals(1, userId);
    }

    @Test
    void testCheckAuth_WithoutRequiredRole() {
        // Given
        List<AccessRole> requiredRoles = Collections.singletonList(AccessRole.ADMIN);
        when(keycloakJwtService.extractEmail(mockJwt)).thenReturn("test@example.com");
        when(authUserRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));

        // When & Then
        assertThrows(UserAuthorizationException.class,
            () -> authenticationService.checkAuth(mockJwt, requiredRoles));
    }

    @Test
    void testCheckAuth_NoRoleRequired() {
        // Given
        when(keycloakJwtService.extractEmail(mockJwt)).thenReturn("test@example.com");
        when(authUserRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));

        // When
        Integer userId = authenticationService.checkAuth(mockJwt);

        // Then
        assertNotNull(userId);
        assertEquals(1, userId);
    }

    @Test
    void testCheckAuth_MultipleRoles_OneMatches() {
        // Given
        List<AccessRole> requiredRoles = Arrays.asList(AccessRole.ADMIN, AccessRole.DATA_SUBMITTER, AccessRole.OFFICER);
        when(keycloakJwtService.extractEmail(mockJwt)).thenReturn("test@example.com");
        when(authUserRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));

        // When
        Integer userId = authenticationService.checkAuth(mockJwt, requiredRoles);

        // Then
        assertNotNull(userId);
        assertEquals(1, userId);
    }
}

