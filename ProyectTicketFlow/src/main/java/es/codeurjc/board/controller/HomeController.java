package es.codeurjc.board.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/login")
    public String mostrarLogin() {
        return "login";
    }

    @GetMapping("/register")
    public String mostrarRegister() {
        return "register";
    }

    @GetMapping("/")
    public String mostrarIndex() {
        return "index";
    }

    @GetMapping("/profile")
    public String mostrarProfile() {
        return "profile";
    }

    @GetMapping("/edit-profile")
    public String mostrarEditProfile() {
        return "edit-profile";
    }

    @GetMapping("/admin")
    public String mostrarAdmin() {
        return "admin";
    }




}