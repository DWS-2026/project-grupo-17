package es.codeurjc.board.security;

import es.codeurjc.board.security.jwt.JwtRequestFilter;
import es.codeurjc.board.security.jwt.UnauthorizedHandlerJwt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
public class ApiSecurityConfig {

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Autowired
    private UnauthorizedHandlerJwt unauthorizedHandlerJwt;

    @Bean
    @Order(1)
    public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {

        http
                .securityMatcher("/api/v1/**")
                .exceptionHandling(handling -> handling.authenticationEntryPoint(unauthorizedHandlerJwt));

        http.authorizeHttpRequests(authorize -> authorize
                // API REST AUTH
                .requestMatchers("/api/v1/auth/login").permitAll()
                .requestMatchers("/api/v1/auth/signup").permitAll()
                .requestMatchers("/api/v1/auth/refresh").permitAll()
                .requestMatchers("/api/v1/auth/logout").authenticated()
                .requestMatchers("/api/v1/users/*").authenticated()

                // API REST PUBLIC GET
                .requestMatchers(HttpMethod.GET, "/api/v1/clubs/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/events/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/tickets/**").permitAll()

                // API REST ADMIN
                .requestMatchers(HttpMethod.POST, "/api/v1/clubs/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/v1/clubs/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/clubs/**").hasRole("ADMIN")

                .requestMatchers(HttpMethod.POST, "/api/v1/events/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/v1/events/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/events/**").hasRole("ADMIN")

                .requestMatchers(HttpMethod.POST, "/api/v1/tickets/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/v1/tickets/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/tickets/**").hasRole("ADMIN")

                .requestMatchers(HttpMethod.GET, "/api/v1/users").hasRole("ADMIN")

                // RESTO API
                .anyRequest().permitAll()
        );

        http
                .exceptionHandling(exception -> exception

                        // 401 - No autenticado
                        .authenticationEntryPoint((request, response, authException) -> {

                            if (request.getRequestURI().startsWith("/api/")) {
                                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                                response.setContentType("application/json");
                                response.getWriter().write("""
                                    {
                                        "status": 401,
                                        "error": "Unauthorized",
                                        "message": "No autenticado"
                                    }
                                """);
                            } else {
                                response.sendRedirect("/login");
                            }
                        })

                        // 403 - Sin permisos
                        .accessDeniedHandler((request, response, accessDeniedException) -> {

                            if (request.getRequestURI().startsWith("/api/")) {
                                response.setStatus(HttpStatus.FORBIDDEN.value());
                                response.setContentType("application/json");
                                response.getWriter().write("""
                                    {
                                        "status": 403,
                                        "error": "Forbidden",
                                        "message": "No tienes permisos"
                                    }
                                """);
                            } else {
                                response.sendRedirect("/error-403");
                            }
                        })
                );

        // CSRF desactivado solo para API REST
        http.csrf(csrf -> csrf.disable());

        return http.build();
    }
}