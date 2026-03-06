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

    @GetMapping("/discotecas/{id}")
    public String detailsDiscoteca(@PathVariable long id, Model model) {
        Discoteca discoteca = discotecaService.findById(id);
        model.addAttribute("discoteca", discoteca);
        return "detalles-discoteca";
    }

    @GetMapping("/discotecas/edit-discoteca/{id}")
    public String editDiscotecaForm(@PathVariable long id, Model model) {
        Discoteca discoteca = discotecaService.findById(id);
        model.addAttribute("discoteca", discoteca);
        return "edit-discoteca";
    }

    // --- AQUÍ ESTÁ EL CAMBIO PRINCIPAL ---
    @PostMapping("/discotecas/create-discotecas")
    public String createDiscoteca(@RequestParam String name,
                                  @RequestParam MultipartFile image,
                                  @RequestParam String calle,         // Nuevo parámetro
                                  @RequestParam String descripcion)   // Nuevo parámetro
                                  throws IOException {
        
        // Ahora pasamos los 4 datos al servicio
        discotecaService.save(name, image, calle, descripcion);
        
        return "redirect:/discotecas";
    }
    // -------------------------------------

    @GetMapping("/discotecas/{id}/image")
    @ResponseBody
    public ResponseEntity<byte[]> showImage(@PathVariable long id) {
        Discoteca d = discotecaService.findById(id);
        if (d == null || d.getImage() == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_TYPE, "image/jpeg") 
                .body(d.getImage());
    }

    @PostMapping("/discotecas/{id}/delete")
    public String deleteDiscoteca(@PathVariable long id) {

        discotecaService.delete(id);

        return "redirect:/discotecas";
    }
}