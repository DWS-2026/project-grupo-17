package es.codeurjc.board.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;

@ControllerAdvice
/**
 * Advice global para inyectar en todas las vistas datos comunes de sesion.
 */
public class GlobalControllerAdvice {

    @ModelAttribute
    // Se ejecuta antes de cada controlador y evita repetir este bloque en cada clase.
    public void addAttributes(Model model, HttpServletRequest request) {

        Principal principal = request.getUserPrincipal();

        if (principal != null) {
            model.addAttribute("email", principal.getName());
            model.addAttribute("admin", request.isUserInRole("ADMIN"));
        } else {
            model.addAttribute("email", null);
            model.addAttribute("admin", false);
        }
    }
}
