package es.codeurjc.board.controller;

import es.codeurjc.board.exception.AccessDeniedException;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
/**
 * Manejador global de excepciones para devolver vistas de error amigables.
 */
public class GlobalExceptionHandler {

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler({AccessDeniedException.class, org.springframework.security.access.AccessDeniedException.class})
    // Captura errores de permisos y devuelve la pagina 403.
    public String handleAccessDenied(Exception exception, Model model) {
        model.addAttribute("error", exception.getMessage());
        return "error-403";
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NoResourceFoundException.class)
    // Captura recursos no existentes y devuelve la pagina 404.
    public String handleNotFound(NoResourceFoundException exception, Model model) {
        model.addAttribute("error", exception.getMessage());
        return "404";
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    // Fallback general para cualquier error no contemplado.
    public String handleException(Exception e, Model model) {
        model.addAttribute("error", e.getMessage());
        model.addAttribute("stackTrace", e.getStackTrace());
        // e.printStackTrace(); // Util solo para depuracion local.
        return "error";
    }
}
