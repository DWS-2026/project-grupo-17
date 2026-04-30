package es.codeurjc.board.security;

import es.codeurjc.board.security.jwt.JwtRequestFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    RepositoryUserDetails userDetailsService;

    @Autowired
    JwtRequestFilter jwtRequestFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider =
                new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.authenticationProvider(authenticationProvider());

        // Filtro JWT para API REST
        http.addFilterBefore(jwtRequestFilter,
                UsernamePasswordAuthenticationFilter.class);

        http.authorizeHttpRequests(authorize -> authorize


                        // WEB


                        // ADMIN - DISCOTECAS
                        .requestMatchers("/discotecas/create-discotecas").hasRole("ADMIN")
                        .requestMatchers("/discotecas/edit/**").hasRole("ADMIN")
                        .requestMatchers("/discotecas/delete/**").hasRole("ADMIN")

                        // ADMIN - EVENTOS
                        .requestMatchers("/discotecas/*/eventos/create").hasRole("ADMIN")
                        .requestMatchers("/eventos/*/edit").hasRole("ADMIN")
                        .requestMatchers("/eventos/*/delete").hasRole("ADMIN")

                        // ADMIN - ENTRADAS
                        .requestMatchers("/eventos/*/entradas/create").hasRole("ADMIN")
                        .requestMatchers("/entradas/create-ticket").hasRole("ADMIN")
                        .requestMatchers("/entradas/*/edit").hasRole("ADMIN")
                        .requestMatchers("/entradas/*/delete").hasRole("ADMIN")

                        // ADMIN PANEL
                        .requestMatchers("/admin").hasRole("ADMIN")
                        .requestMatchers("/admin/users/*/profile").hasRole("ADMIN")

                        // PUBLICO WEB
                        .requestMatchers("/", "/login", "/register", "/css/*", "/images/*").permitAll()
                        .requestMatchers("/user/*/avatar").permitAll()

                        // USER LOGUEADO WEB
                        .requestMatchers("/profile", "/edit-profile", "/entradas/*/pago", "/mis-entradas").authenticated()


                        // API REST AUTH

                        .requestMatchers("/api/v1/auth/login").permitAll()
                        .requestMatchers("/api/v1/auth/signup").permitAll()
                        .requestMatchers("/api/v1/auth/refresh").permitAll()
                        .requestMatchers("/api/v1/auth/logout").authenticated()


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

                        .requestMatchers("/api/v1/users/**").hasRole("ADMIN")

                        // RESTO
                        .anyRequest().permitAll()
                )

                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )

                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .permitAll()
                )

                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .accessDeniedPage("/error-403")
                )

                // CSRF desactivado solo para API REST
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"));

        return http.build();
    }
}