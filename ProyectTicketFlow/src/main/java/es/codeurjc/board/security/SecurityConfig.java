package es.codeurjc.board.security;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz.anyRequest().permitAll())
            .csrf(withDefaults())
            .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()))
            .formLogin(formLogin -> formLogin.disable())
            .httpBasic(httpBasic -> httpBasic.disable());
        return http.build();
    }

    @Bean
    public FilterRegistrationBean<AuthenticationFilter> authenticationFilterRegistration(AuthenticationFilter authFilter) {
        FilterRegistrationBean<AuthenticationFilter> registration = new FilterRegistrationBean<>(authFilter);
        registration.addUrlPatterns("/*");
        registration.setOrder(0);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<AdminFilter> adminFilterRegistration(AdminFilter adminFilter) {
        FilterRegistrationBean<AdminFilter> registration = new FilterRegistrationBean<>(adminFilter);
        registration.addUrlPatterns(
            "/discotecas/create-discotecas",
            "/discotecas/edit-discoteca/*",
            "/discotecas/edit/*",
            "/discotecas/delete/*",
            "/discotecas/*/eventos/create",
            "/eventos/create-event",
            "/eventos/*/edit",
            "/eventos/*/delete",
            "/admin"
        );
        registration.setOrder(1);
        return registration;
    }
}
