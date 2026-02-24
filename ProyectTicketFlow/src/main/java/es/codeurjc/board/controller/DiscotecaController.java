package es.codeurjc.board.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import es.codeurjc.board.service.DiscotecaService;

@Controller
public class DiscotecaController {

    @Autowired
    private DiscotecaService discotecaService;

    @GetMapping("/discotecas")
    public String showDiscotecas(Model model) {

        model.addAttribute("discotecas", discotecaService.findAll());

        return "discotecas";
    }

    @GetMapping("/discotecas/new")
    public String newDiscotecaForm() {
        return "create-discotecas";
    }

    @PostMapping("/discotecas/new")
    public String createDiscoteca(
            @RequestParam String name,
            @RequestParam MultipartFile image
    ) throws IOException {

        discotecaService.save(name, image);

        return "redirect:/discotecas";
    }

    @GetMapping("/discotecas/{id}/image")
    @ResponseBody
    public byte[] showImage(@PathVariable long id) {
        return discotecaService.findById(id).getImage();
    }

    @PostMapping("/discotecas/{id}/delete")
    public String deleteDiscoteca(@PathVariable long id) {

        discotecaService.delete(id);

        return "redirect:/discotecas";
    }
}