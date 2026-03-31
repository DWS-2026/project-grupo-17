package es.codeurjc.board.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    RepositoryUserDetails userDetailsService;

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
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.authenticationProvider(authenticationProvider());

        http
                .authorizeHttpRequests(authorize -> authorize

                        // 🔥 H2 CONSOLE
                        .requestMatchers("/h2-console/**").permitAll()

                        // 🔴 ADMIN - DISCOTECAS
                        .requestMatchers("/discotecas/create-discotecas").hasRole("ADMIN")
                        .requestMatchers("/discotecas/edit/**").hasRole("ADMIN")
                        .requestMatchers("/discotecas/delete/**").hasRole("ADMIN")

                        // 🔴 ADMIN - EVENTOS
                        .requestMatchers("/discotecas/*/eventos/create").hasRole("ADMIN")
                        .requestMatchers("/eventos/*/edit").hasRole("ADMIN")
                        .requestMatchers("/eventos/*/delete").hasRole("ADMIN")

                        // 🔴 ADMIN - ENTRADAS
                        .requestMatchers("/eventos/*/entradas/create").hasRole("ADMIN")
                        .requestMatchers("/entradas/create-ticket").hasRole("ADMIN")
                        .requestMatchers("/entradas/*/edit").hasRole("ADMIN")
                        .requestMatchers("/entradas/*/delete").hasRole("ADMIN")

                        .requestMatchers("/admin").hasRole("ADMIN")
                        .requestMatchers("/user/*/avatar").permitAll()


                        // 🔵 USUARIO LOGUEADO
                        .requestMatchers("/profile", "/edit-profile", "/entradas/*/pago", "/mis-entradas").authenticated()

                        // 🟢 PÚBLICO
                        .requestMatchers("/", "/login", "/register", "/css/**", "/images/**").permitAll()

                        // RESTO
                        .anyRequest().permitAll()
                )

                // 🔐 LOGIN
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )

                // 🚪 LOGOUT
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .permitAll()
                )

                // 🔥 CSRF (IMPORTANTE PARA H2)
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/h2-console/**")
                )

                // 🔥 H2 necesita iframes
                .headers(headers -> headers
                        .frameOptions(frame -> frame.disable())
                );

        return http.build();
    }
}