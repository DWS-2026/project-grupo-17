package es.codeurjc.board.controller;

import java.io.IOException;

import es.codeurjc.board.model.Discoteca;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

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

    @GetMapping("/discotecas/create-discotecas")
    public String newDiscotecaForm() {

        return "create-discotecas";
    }

    @PostMapping("/discotecas/create-discotecas")
    public String createDiscoteca(@RequestParam String name,
                                  @RequestParam MultipartFile image) throws IOException {
        discotecaService.save(name, image);
        return "redirect:/discotecas";
    }

    @GetMapping("/discotecas/{id}/image")
    @ResponseBody
    public ResponseEntity<byte[]> showImage(@PathVariable long id) {
        Discoteca d = discotecaService.findById(id);
        if (d == null || d.getImage() == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_TYPE, "image/jpeg") // importante
                .body(d.getImage());
    }

    @PostMapping("/discotecas/{id}/delete")
    public String deleteDiscoteca(@PathVariable long id) {

        discotecaService.delete(id);

        return "redirect:/discotecas";
    }
}