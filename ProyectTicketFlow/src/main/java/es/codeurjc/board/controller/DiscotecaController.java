package es.codeurjc.board.controller;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

// Importaciones de tus modelos y servicios
import es.codeurjc.board.model.Discoteca;
import es.codeurjc.board.model.Evento;
import es.codeurjc.board.repositories.DiscotecaRepository;
import es.codeurjc.board.service.DiscotecaService;
import es.codeurjc.board.service.EventoService;
import es.codeurjc.board.service.UserSession;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import java.nio.file.Files;


@Controller
public class DiscotecaController {

    @Autowired
    private DiscotecaService discotecaService;

    // Inyectamos el EventoService para que el nuevo método funcione sin dar error rojo
    @Autowired
    private EventoService eventoService;

    @Autowired
    private DiscotecaRepository discotecaRepository;

    @Autowired
    private UserSession userSession;

    @PostConstruct
    public void init() throws IOException {
        if (discotecaRepository.count() == 0) { // Evita dup



            Discoteca d1 = new Discoteca();
            d1.setName("Nuit");
            d1.setCalle("Calle Mayor 10");
            d1.setDescripcion("Discoteca con música electrónica");
            d1.setImage(null);

            Discoteca d2 = new Discoteca();
            d2.setName("La Riviera");
            d2.setCalle("Avenida del Sol 25");
            d2.setDescripcion("Ambiente chill y cocktails");
            d2.setImage(null);

            discotecaRepository.save(d1);
            discotecaRepository.save(d2);
        }
    }


    @GetMapping("/discotecas")
    public String showDiscotecas(Model model) {
        model.addAttribute("discotecas", discotecaService.findAll());
        model.addAttribute("isAdmin", userSession.isAdmin());
        return "discotecas";
    }

    @GetMapping("/discotecas/create-discotecas")
    public String newDiscotecaForm(Model model) {
        if (!userSession.isAdmin()) {
            model.addAttribute("error", "Solo los administradores pueden crear discotecas");
            return "redirect:/discotecas";
        }
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
        if (!userSession.isAdmin()) {
            model.addAttribute("error", "Solo los administradores pueden editar discotecas");
            return "redirect:/discotecas";
        }
        Discoteca discoteca = discotecaService.findById(id);
        model.addAttribute("discoteca", discoteca);
        return "edit-discoteca";
    }

    @PostMapping("/discotecas/edit/{id}")
    public String actualizarDiscoteca(
            @PathVariable Long id,
            @RequestParam String name,
            @RequestParam String calle,
            @RequestParam String descripcion,
            @RequestParam MultipartFile image,
            Model model) throws IOException {

        if (!userSession.isAdmin()) {
            model.addAttribute("error", "Solo los administradores pueden editar discotecas");
            return "redirect:/discotecas";
        }

        discotecaService.update(id,name, image, calle, descripcion);

        return "redirect:/discotecas";
    }

    @PostMapping("/discotecas/create-discotecas")
    public String createDiscoteca(@RequestParam String name,
                                  @RequestParam MultipartFile image,
                                  @RequestParam String calle,
                                  @RequestParam String descripcion,
                                  Model model)
            throws IOException {

        if (!userSession.isAdmin()) {
            model.addAttribute("error", "Solo los administradores pueden crear discotecas");
            return "redirect:/discotecas";
        }

        // Pasamos los 4 datos al servicio
        discotecaService.save(name, image, calle, descripcion);

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
                .header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
                .body(d.getImage());
    }

    @PostMapping("/discotecas/delete/{id}")
    public String deleteDiscoteca(@PathVariable long id, Model model) {
        if (!userSession.isAdmin()) {
            model.addAttribute("error", "Solo los administradores pueden eliminar discotecas");
            return "redirect:/discotecas";
        }
        discotecaService.delete(id);
        return "redirect:/discotecas";
    }

}