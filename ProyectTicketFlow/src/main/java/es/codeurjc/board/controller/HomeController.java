package es.codeurjc.board.controller;

import es.codeurjc.board.service.UserService;
import es.codeurjc.board.service.UserSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserSession userSession;

    @GetMapping("/")
    public String mostrarIndex() {
        return "index";
    }

    @GetMapping("/edit-profile")
    public String mostrarEditProfile() {
        return "edit-profile";
    }

    @GetMapping("/admin")
    public String mostrarAdmin(Model model) {
        if (!userSession.isAdmin()) {
            model.addAttribute("error", "Solo los administradores pueden acceder aquí");
            return "redirect:/";
        }
        String emailActual = userSession.getUser();
        model.addAttribute("usuarios", userService.findAll().stream()
                .filter(u -> !u.getEmail().equals(emailActual))
                .toList());
        return "admin";
    }
}