package ex.org.project.userservice.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * Service for authenticating users from JWT tokens and checking authorization
 * Replaces the old RAS-based UserAuthService
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserAuthenticationService {

    private final AuthUserRepository authUserRepository;
    private final KeycloakJwtService keycloakJwtService;

    /**
     * Get authenticated user from JWT token
     * @param jwt JWT token from SecurityContext
     * @return AuthUser entity
     * @throws UserNotFoundException if user not found in database
     */
    public AuthUser getAuthenticatedUser(Jwt jwt) {
        String email = keycloakJwtService.extractEmail(jwt);
        return authUserRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
    }

    /**
     * Get authenticated user ID from JWT token
     * @param jwt JWT token from SecurityContext
     * @return User ID
     * @throws UserNotFoundException if user not found in database
     */
    public Integer getAuthenticatedUserId(Jwt jwt) {
        return getAuthenticatedUser(jwt).getId();
    }

    /**
     * Check if user has any of the required roles
     * @param user AuthUser entity
     * @param requiredRoles List of required access roles
     * @return true if user has any of the required roles
     */
    public boolean hasAnyRole(AuthUser user, List<AccessRole> requiredRoles) {
        if (requiredRoles == null || requiredRoles.isEmpty()) {
            return true;  // No specific role required
        }

        List<String> userRoleNames = user.getRoles().stream()
                .map(AuthRole::getName)
                .toList();

        for (AccessRole requiredRole : requiredRoles) {
            if (userRoleNames.contains(requiredRole.label)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check authentication and authorization
     * @param jwt JWT token
     * @param requiredRoles Required roles (empty list = any authenticated user)
     * @return User ID if authorized
     * @throws UserAuthenticationException if user not found
     * @throws UserAuthorizationException if user doesn't have required role
     */
    public Integer checkAuth(Jwt jwt, List<AccessRole> requiredRoles) {
        AuthUser user = getAuthenticatedUser(jwt);

        if (!hasAnyRole(user, requiredRoles)) {
            log.warn("User {} attempted access without required roles. Required: {}, User has: {}",
                    user.getEmail(), requiredRoles, user.getRoles());
            throw new UserAuthorizationException("User does not have the necessary role for access");
        }

        return user.getId();
    }

    /**
     * Check authentication only (no role check)
     * @param jwt JWT token
     * @return User ID
     * @throws UserAuthenticationException if user not found
     */
    public Integer checkAuth(Jwt jwt) {
        return checkAuth(jwt, Collections.emptyList());
    }
}

