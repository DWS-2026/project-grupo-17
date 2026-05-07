package es.codeurjc.board.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Autowired
    RepositoryUserDetails userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain webFilterChain(HttpSecurity http) throws Exception {

        http.authenticationProvider(authenticationProvider());

        http.authorizeHttpRequests(authorize -> authorize

                // ADMIN - DISCOTECAS
                .requestMatchers("/discotecas/create-discotecas").hasRole("ADMIN")
                .requestMatchers("/discotecas/edit-discotecas/**").hasRole("ADMIN")
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

                // RESTO
                .anyRequest().permitAll())

                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/login?error=true")
                        .permitAll())

                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .permitAll())

                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .accessDeniedPage("/error-403"));

        return http.build();
    }
}
