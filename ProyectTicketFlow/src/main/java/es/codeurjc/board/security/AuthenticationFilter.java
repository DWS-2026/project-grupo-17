package es.codeurjc.board.security;

import es.codeurjc.board.service.UserSession;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AuthenticationFilter implements Filter {

    @Autowired
    private UserSession userSession;

    private static final String[] PUBLIC_URLS = {
        "/login", "/register", "/logout", "/", "/css", "/images", "/js", "/h2-console"
    };

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String path = httpRequest.getRequestURI();

        // Permitir rutas públicas
        if (isPublicUrl(path)) {
            chain.doFilter(request, response);
            return;
        }

        // Si el usuario no está logueado, redirigir a login
        if (userSession.getUser() == null) {
            httpResponse.sendRedirect("/login");
            return;
        }

        // Usuario autenticado, permitir la solicitud
        chain.doFilter(request, response);
    }

    private boolean isPublicUrl(String path) {
        for (String publicUrl : PUBLIC_URLS) {
            if (path.startsWith(publicUrl)) {
                return true;
            }
        }
        return false;
    }
}
