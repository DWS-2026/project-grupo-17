package es.codeurjc.board.controller;

import es.codeurjc.board.exception.AccessDeniedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Map;

@ControllerAdvice
/**
 * Manejador global de excepciones para devolver vistas de error amigables.
 */
public class GlobalExceptionHandler {

    private boolean isApi(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String accept = request.getHeader("Accept");

        return uri.startsWith("/api/")
                || (accept != null && accept.contains("application/json"));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public Object handle404(HttpServletRequest request, HttpServletResponse response) {

        String uri = request.getRequestURI();

        if (isApi(request)) {
            response.setStatus(404);
            return ResponseEntity.status(404).body(Map.of(
                    "status", 404,
                    "error", "Not Found",
                    "message", "Recurso no encontrado",
                    "path", uri
            ));
        }

        response.setStatus(404);
        return new ModelAndView("404");
    }

    @ExceptionHandler({AccessDeniedException.class})
    public Object handle403(Exception ex, HttpServletRequest request, HttpServletResponse response) {

        if (isApi(request)) {
            response.setStatus(403);
            return ResponseEntity.status(403).body(Map.of(
                    "status", 403,
                    "error", "Forbidden",
                    "message", "No tienes permisos",
                    "path", request.getRequestURI()
            ));
        }

        response.setStatus(403);
        return new ModelAndView("403");
    }

    @ExceptionHandler(Exception.class)
    public Object handle500(Exception ex, HttpServletRequest request, HttpServletResponse response) {

        ex.printStackTrace();

        if (isApi(request)) {
            response.setStatus(500);
            return ResponseEntity.status(500).body(Map.of(
                    "status", 500,
                    "error", "Internal Server Error",
                    "message", "Error interno del servidor",
                    "path", request.getRequestURI()
            ));
        }

        response.setStatus(500);
        return new ModelAndView("500");
    }
}
