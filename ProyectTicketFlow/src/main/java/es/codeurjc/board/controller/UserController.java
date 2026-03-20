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
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserSession userSession;

    @GetMapping("/login")
    public String getLogin(Model model) {
        return "login";
    }

    @PostMapping("/login")
    public String postLogin(
            @RequestParam String email,
            @RequestParam String password,
            Model model) {

        Optional<User> user = userService.findByEmail(email);
        if (user.isPresent() && user.get().getPassword().equals(password)) {
            userSession.setUser(email);
            userSession.setUserId(user.get().getId());
            userSession.setAdmin(user.get().isAdmin());
            return "redirect:/";
        } else {
            model.addAttribute("error", "Email o contraseña incorrectos");
            return "login";
        }
    }

    @GetMapping("/register")
    public String getRegister(Model model) {
        return "register";
    }

    @PostMapping("/register")
    public String postRegister(
            @RequestParam String nombre,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String fechaNacimiento,
            @RequestParam(required = false) MultipartFile avatar,
            Model model) {

        try {
            // Verificar si el email ya existe
            Optional<User> usuarioExistente = userService.findByEmail(email);
            if (usuarioExistente.isPresent()) {
                model.addAttribute("error", "El email ya está registrado");
                return "register";
            }

            // Convertir string a LocalDate
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate fecha = LocalDate.parse(fechaNacimiento, formatter);

            // Guardar el usuario
            userService.save(nombre, email, password, fecha, avatar);

            // Iniciar sesión automáticamente
            Optional<User> usuarioGuardado = userService.findByEmail(email);
            if (usuarioGuardado.isPresent()) {
                userSession.setUser(email);
                userSession.setUserId(usuarioGuardado.get().getId());
                userSession.setAdmin(usuarioGuardado.get().isAdmin());
            }

            return "redirect:/";
        } catch (IOException e) {
            model.addAttribute("error", "Error al guardar la imagen");
            return "register";
        } catch (Exception e) {
            model.addAttribute("error", "Error al registrar el usuario");
            return "register";
        }
    }

    @GetMapping("/logout")
    public String logout() {
        userSession.logout();
        return "redirect:/";
    }

    @GetMapping("/profile")
    public String getProfile(Model model) {
        String email = userSession.getUser();
        if (email != null) {
            Optional<User> user = userService.findByEmail(email);
            if (user.isPresent()) {
                model.addAttribute("user", user.get());
                return "profile";
            }
        }
        return "redirect:/login";
    }

    @PostMapping("/profile/update")
    public String updateProfile(
            @RequestParam Long id,
            @RequestParam String nombre,
            @RequestParam String email,
            @RequestParam String fechaNacimiento,
            @RequestParam(required = false) MultipartFile avatar,
            Model model) {

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate fecha = LocalDate.parse(fechaNacimiento, formatter);

            userService.update(id, nombre, email, fecha, avatar);

            return "redirect:/profile";
        } catch (IOException e) {
            model.addAttribute("error", "Error al guardar la imagen");
            return "profile";
        } catch (Exception e) {
            model.addAttribute("error", "Error al actualizar el perfil");
            return "profile";
        }
    }
}
