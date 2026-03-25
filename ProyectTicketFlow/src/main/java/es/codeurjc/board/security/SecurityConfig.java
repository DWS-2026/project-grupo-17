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
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.authenticationProvider(authenticationProvider());

        http
            .authorizeHttpRequests(authorize -> authorize
                
                // 1. ZONA EXCLUSIVA PARA ADMINISTRADORES (Crear y editar)
                // Usamos hasAuthority en lugar de hasRole para evitar el problema del prefijo ROLE_
                .requestMatchers("/admin").hasAnyRole("ADMIN")
                
                //.requestMatchers("/create-discotecas", "/create-event", "/entradas/create-ticket").hasAuthority("ADMIN")
                .requestMatchers("/entradas/create-ticket").hasAnyRole("ADMIN")
				// Añadimos "/**" por si las URLs de edición llevan una ID al final, ej: /edit-event/5
                .requestMatchers("/edit-discoteca/**", "/edit-event/**", "/edit-ticket/**").hasAuthority("ADMIN")
                .requestMatchers("/favicon.ico").permitAll()
                // 2. ZONA PARA USUARIOS REGISTRADOS (Cualquiera que haya hecho login)
                .requestMatchers("/profile", "/edit-profile").authenticated()
                
                // 3. ZONA PÚBLICA (Cualquiera, incluso sin hacer login)
                .anyRequest().permitAll()
            )
            .formLogin(formLogin -> formLogin
                .loginPage("/login")
                .failureUrl("/loginerror") // O donde manejes tu error
                .defaultSuccessUrl("/")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .permitAll()
            );

        return http.build();
    }
}