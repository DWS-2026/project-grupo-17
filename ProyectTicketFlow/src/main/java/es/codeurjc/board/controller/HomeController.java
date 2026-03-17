package es.codeurjc.board.controller;

import es.codeurjc.board.model.User;
import es.codeurjc.board.service.UserService;
import es.codeurjc.board.service.UserSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

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
    public String mostrarEditProfile(Model model) {
        String email = userSession.getUser();
        if (email != null) {
            Optional<User> user = userService.findByEmail(email);
            if (user.isPresent()) {
                model.addAttribute("user", user.get());
                return "edit-profile";
            }
        }
        return "redirect:/login";
    }

    @PostMapping("/edit-profile")
    public String updateProfile(
            @RequestParam String nombre,
            @RequestParam String email,
            @RequestParam String fechaNacimiento,
            @RequestParam(required = false) MultipartFile avatar,
            Model model) {

        try {
            String emailActual = userSession.getUser();
            Optional<User> user = userService.findByEmail(emailActual);
            
            if (user.isPresent()) {
                Long userId = user.get().getId();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate fecha = LocalDate.parse(fechaNacimiento, formatter);
                
                userService.update(userId, nombre, email, fecha, avatar);
                
                // Actualizar sesión si el email cambió
                if (!email.equals(emailActual)) {
                    userSession.setUser(email);
                }
                
                return "redirect:/profile";
            }
        } catch (IOException e) {
            model.addAttribute("error", "Error al guardar la imagen");
        } catch (Exception e) {
            model.addAttribute("error", "Error al actualizar el perfil");
        }
        
        return "redirect:/edit-profile";
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