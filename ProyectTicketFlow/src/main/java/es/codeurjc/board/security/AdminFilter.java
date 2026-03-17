package es.codeurjc.board.security;

import es.codeurjc.board.service.UserSession;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AdminFilter implements Filter {

    @Autowired
    private UserSession userSession;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Si el usuario no es admin, redirigir a error-403
        if (!userSession.isAdmin()) {
            httpResponse.sendRedirect("/error-403");
            return;
        }

        // Si es admin, permitir la solicitud
        chain.doFilter(request, response);
    }
}
