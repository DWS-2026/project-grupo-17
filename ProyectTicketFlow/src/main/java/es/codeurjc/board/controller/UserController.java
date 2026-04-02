package es.codeurjc.board.controller;

import es.codeurjc.board.UserDTO;
import es.codeurjc.board.model.User;
import es.codeurjc.board.service.UserService;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.sql.Blob;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Controller
public class UserController {

    @Autowired
    private UserService userService;


    // 🔹 LOGIN
    @GetMapping("/login")
    public String getLogin(@RequestParam(required = false) String error, Model model) {

        if (error != null) {
            model.addAttribute("error", "Usuario no registrado o contraseña incorrecta");
        }

        return "login";
    }

    // 🔹 REGISTER (GET)
    @GetMapping("/register")
    public String getRegister() {
        return "register";
    }

    // 🔹 REGISTER (POST)
    @PostMapping("/register")
    public String postRegister(
            @RequestParam String nombre,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String fechaNacimiento,
            @RequestParam(required = false) MultipartFile avatar,
            Model model) {

        try {

            //  VALIDACIONES BACKEND

            if (nombre == null || nombre.isBlank()) {
                model.addAttribute("error", "El nombre es obligatorio");
                return "register";
            }

            if (email == null || email.isBlank()) {
                model.addAttribute("error", "El email es obligatorio");
                return "register";
            }

            if (!email.contains("@")) {
                model.addAttribute("error", "El email no es válido");
                return "register";
            }

            if (password == null || password.length() < 4) {
                model.addAttribute("error", "La contraseña debe tener al menos 4 caracteres");
                return "register";
            }

            if (fechaNacimiento == null || fechaNacimiento.isBlank()) {
                model.addAttribute("error", "La fecha de nacimiento es obligatoria");
                return "register";
            }

            //  EMAIL YA EXISTE
            Optional<User> usuarioExistente = userService.findByEmail(email);
            if (usuarioExistente.isPresent()) {
                model.addAttribute("error", "El email ya está registrado");
                return "register";
            }

            //  PARSE FECHA
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate fecha = LocalDate.parse(fechaNacimiento, formatter);

            //  GUARDAR
            userService.save(nombre, email, password, fecha, avatar);

            return "redirect:/login";

        } catch (IOException e) {
            model.addAttribute("error", "Error al guardar la imagen");
            return "register";
        } catch (Exception e) {
            model.addAttribute("error", "Error al registrar el usuario");
            return "register";
        }
    }

    // 🔹 PERFIL
    @GetMapping("/profile")
    public String getProfile(Model model, Principal principal) {

        if (principal != null && !principal.getName().equals("anonymousUser")) {
            String email = principal.getName();

            Optional<User> user = userService.findByEmail(email);
            if (user.isPresent()) {
                model.addAttribute("user", user.get());
                boolean profileUserAdmin = user.get().getRoles() != null && user.get().getRoles().contains("ADMIN");
                model.addAttribute("profileUserAdmin", profileUserAdmin);
                model.addAttribute("canEditProfile", true);
                model.addAttribute("canViewAdminPanel", profileUserAdmin);
                model.addAttribute("showMisEntradasButton", !profileUserAdmin);
                model.addAttribute("adminViewingOtherProfile", false);

                model.addAttribute("entradas", user.get().getEntradasCompradas());
                return "profile";
            }
        }

        return "redirect:/login";
    }

    // 🔹 EDIT PROFILE (GET)
    @GetMapping("/edit-profile")
    public String mostrarEditProfile(Model model, Principal principal) {

        if (principal != null && !principal.getName().equals("anonymousUser")) {
            String email = principal.getName();

            Optional<User> user = userService.findByEmail(email);
            if (user.isPresent()) {
                model.addAttribute("user", user.get());
                return "edit-profile";
            }
        }

        return "redirect:/login";
    }

    // 🔹 EDIT PROFILE (POST) ✅ MÉTODO BUENO (SEGURO)
    @PostMapping("/edit-profile")
    public String updateProfile(
            @RequestParam String nombre,
            @RequestParam String email,
            @RequestParam String fechaNacimiento,
            @RequestParam(required = false) MultipartFile avatar,
            Model model,
            Principal principal) {

        try {
            String emailActual = principal.getName();

            Optional<User> user = userService.findByEmail(emailActual);

            if (user.isPresent()) {
                Long userId = user.get().getId();

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate fecha = LocalDate.parse(fechaNacimiento, formatter);

                userService.update(userId, nombre, email, fecha, avatar);

                return "redirect:/profile";
            }

        } catch (IOException e) {
            model.addAttribute("error", "Error al guardar la imagen");
        } catch (Exception e) {
            model.addAttribute("error", "Error al actualizar el perfil");
        }

        return "redirect:/edit-profile";
    }

    // 🔹 ADMIN PANEL
    @GetMapping("/admin")
    public String mostrarAdmin(Model model, Principal principal, HttpServletRequest request) {



        String emailActual = principal.getName();

        model.addAttribute("usuarios", userService.findAll().stream()
                .filter(u -> !u.getEmail().equals(emailActual))
                .map(u -> new UserDTO(
                u.getId(),
                        u.getNombre(),
                        u.getEmail(),
                        u.getFechaNacimiento(),
                        u.getRoles() != null && u.getRoles().contains("ADMIN")
                ))
                .toList());

        return "admin";
    }

    @GetMapping("/admin/users/{id}/profile")
    public String verPerfilUsuarioDesdeAdmin(@PathVariable Long id, Model model) {

        User user = userService.findById(id);

        if (user == null) {
            return "redirect:/admin";
        }

        boolean profileUserAdmin = user.getRoles() != null && user.getRoles().contains("ADMIN");

        model.addAttribute("user", user);
        model.addAttribute("profileUserAdmin", profileUserAdmin);
        model.addAttribute("canEditProfile", false);
        model.addAttribute("canViewAdminPanel", true);
        model.addAttribute("showMisEntradasButton", false);
        model.addAttribute("adminViewingOtherProfile", true);

        model.addAttribute("entradas", user.getEntradasCompradas());

        return "profile";
    }

    @GetMapping("/")
    public String mostrarIndex(Model model, HttpServletRequest request) {

        Principal principal = request.getUserPrincipal();

        if (principal != null && !principal.getName().equals("anonymousUser")) {
            model.addAttribute("email", principal.getName());
            model.addAttribute("admin", request.isUserInRole("ADMIN"));
        }

        return "index";
    }
    @GetMapping("/mis-entradas")
    public String verMisEntradas(Model model, Principal principal) {

        if (principal == null) {
            return "redirect:/login";
        }

        String email = principal.getName();
        User user = userService.findByEmail(email).orElse(null);

        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("entradas", user.getEntradasCompradas());

        return "mis-entradas";
    }

    @GetMapping("/users/{id}/avatar")
    @ResponseBody
    public byte[] getUserAvatar(@PathVariable Long id) throws SQLException, IOException {
        User user = userService.findById(id);

        if (user != null && user.getAvatar() != null) {
            // Obtenemos los bytes de la imagen
            Blob blob = user.getAvatar().getImageFile(); // o getImage() según tu clase Image
            return blob.getBytes(1, (int) blob.length());
        }

        // Imagen por defecto si el usuario no tiene avatar
        Resource resource = new ClassPathResource("/posts/avatar.png");
        return resource.getInputStream().readAllBytes();
    }



}