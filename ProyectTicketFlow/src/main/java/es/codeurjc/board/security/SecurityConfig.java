package es.codeurjc.board.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
/**
 * Configuracion principal de Spring Security.
 * Define autenticacion, autorizacion por rutas, login/logout y manejo de acceso denegado.
 */
public class SecurityConfig {

    @Autowired
    RepositoryUserDetails userDetailsService;

    @Bean
        // Encoder usado para cifrar y verificar contrasenas.
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
        // Proveedor de autenticacion basado en UserDetailsService + PasswordEncoder.
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider =
                new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
    @Bean
	@Order(1)
	public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {

		http.authenticationProvider(authenticationProvider());

		http
				.securityMatcher("/api/**");
				//.exceptionHandling(handling -> handling.authenticationEntryPoint(unauthorizedHandlerJwt));

		http
				.authorizeHttpRequests(authorize -> authorize
						// PRIVATE ENDPOINTS
						// Images
                        //A medida que avance en el proyecto, se pueden ir añadiendo reglas de autorizacion para cada endpoint privado.
						//.requestMatchers(HttpMethod.PUT, "/api/images/*/media").hasRole("USER")
						//.requestMatchers(HttpMethod.DELETE, "/api/books/*/images/*").hasRole("USER")
						// Books
						//.requestMatchers(HttpMethod.POST, "/api/books/**").hasRole("USER")
						//.requestMatchers(HttpMethod.PUT, "/api/books/**").hasRole("USER")
						//.requestMatchers(HttpMethod.DELETE, "/api/books/**").hasRole("ADMIN")
						// Shops
						//.requestMatchers(HttpMethod.PUT, "/api/shops/**").hasRole("ADMIN")
						//.requestMatchers(HttpMethod.PUT, "/api/shops/**").hasRole("ADMIN")
						//.requestMatchers(HttpMethod.DELETE, "/api/shops/**").hasRole("ADMIN")
						// PUBLIC ENDPOINTS
						.anyRequest().permitAll());

		// Disable Form login Authentication
		http.formLogin(formLogin -> formLogin.disable());

		// Disable CSRF protection (it is difficult to implement in REST APIs)
		http.csrf(csrf -> csrf.disable());

		// Disable Basic Authentication
		http.httpBasic(httpBasic -> httpBasic.disable());

		// Stateless session
		http.sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

		// Add JWT Token filter
		//http.addFilterBefore(new JwtRequestFilter(userDetailService, jwtTokenProvider),
				//UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

    @Bean
    @Order(2)
        // Cadena de filtros HTTP: reglas de acceso, formulario de login y logout.
    public SecurityFilterChain webFilterChain(HttpSecurity http) throws Exception {

        http.authenticationProvider(authenticationProvider());

        http
                .authorizeHttpRequests(authorize -> authorize

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

                        .requestMatchers("/admin").hasRole("ADMIN")
                        .requestMatchers("/admin/users/*/profile").hasRole("ADMIN")
                        .requestMatchers("/user/*/avatar").permitAll()


                        // USUARIO LOGUEADO
                        .requestMatchers("/profile", "/edit-profile", "/entradas/*/pago", "/mis-entradas").authenticated()

                        // PUBLICO
                        .requestMatchers("/", "/login", "/register", "/css/**", "/images/**").permitAll()

                        // RESTO
                        .anyRequest().permitAll()
                )

                // LOGIN
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )

                // LOGOUT
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .permitAll()
                )

                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .accessDeniedPage("/error-403")
                )

                .csrf(csrf -> {
                });

        return http.build();
    }
}