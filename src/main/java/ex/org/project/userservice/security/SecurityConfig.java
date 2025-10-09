package ex.org.project.userservice.security;

import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collections;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())  // Disable CSRF for stateless JWT
        .authorizeHttpRequests(auth -> auth
            // Public endpoints for user registration and lookup data
            .requestMatchers(
                "/user/approved-institutions",
                "/user/create-institution",
                "/user/user-registration",
                "/user/referrer-types",
                "/user/researcher-levels",
                "/user/institution-types",
                "/user/states",
                "/user/countries",
                "/user/centers",
                "/support-request/request-types"
            ).permitAll()
            // Actuator endpoints
            .requestMatchers(EndpointRequest.to("health")).permitAll()
            .requestMatchers(EndpointRequest.to("shutdown")).authenticated()
            // All other endpoints require authentication
            .anyRequest().authenticated()
        )
        .oauth2ResourceServer(oauth2 -> oauth2
            .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
        )
        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );

    return http.build();
  }

  @Bean
  public JwtAuthenticationConverter jwtAuthenticationConverter() {
    JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
    // Don't extract roles from JWT - we'll use database roles
    converter.setJwtGrantedAuthoritiesConverter(jwt -> Collections.emptyList());
    return converter;
  }
}

