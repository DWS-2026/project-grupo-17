package es.codeurjc.board.controller;

import es.codeurjc.board.model.Evento;
import es.codeurjc.board.model.User;
import es.codeurjc.board.service.EventoService;
import es.codeurjc.board.service.UserService;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private EventoService eventoService;


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
            // Validar datos de registro
            String error = userService.validarRegistro(nombre, email, password, fechaNacimiento);

            if (error != null) {
                model.addAttribute("error", error);
                return "register";
            }

            // Registrar usuario con validación
            userService.registroConValidacion(nombre, email, password, fechaNacimiento, avatar);

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

    // 🔹 EDIT PROFILE (POST)  MÉTODO BUENO (SEGURO)
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

            if (!user.isPresent()) {
                return "redirect:/login";
            }

            // Validar datos de actualización
            String error = userService.validarActualizacionPerfil(nombre, email, fechaNacimiento);

            if (error != null) {
                model.addAttribute("error", error);
                model.addAttribute("user", user.get());
                return "edit-profile";
            }

            // Actualizar perfil con validación
            userService.actualizarPerfilConValidacion(user.get().getId(), nombre, email, fechaNacimiento, avatar);

            return "redirect:/profile";

        } catch (IOException e) {
            model.addAttribute("error", "Error al guardar la imagen");
            model.addAttribute("user", userService.findByEmail(principal.getName()).orElse(null));
            return "edit-profile";
        } catch (Exception e) {
            model.addAttribute("error", "Error al actualizar el perfil");
            model.addAttribute("user", userService.findByEmail(principal.getName()).orElse(null));
            return "edit-profile";
        }
    }

    // 🔹 ADMIN PANEL
    @GetMapping("/admin")
    public String mostrarAdmin(Model model, Principal principal, HttpServletRequest request) {



        String emailActual = principal.getName();

        model.addAttribute("usuarios", userService.findAllOtherUsersAsDTO(emailActual));

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

        // 🔹 Obtener próximos eventos desde el service
        List<Evento> eventos = eventoService.findFirst3();
        model.addAttribute("eventos", eventos);


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