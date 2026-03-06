package es.codeurjc.board.controller;

import java.io.IOException;

import es.codeurjc.board.model.Discoteca;
import es.codeurjc.board.model.Evento;
import es.codeurjc.board.service.DiscotecaService;
import es.codeurjc.board.service.EventoService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

@Controller
public class EventoController {

    @Autowired
    private EventoService eventoService;

    // AÑADIDO: Necesitamos este servicio para buscar la discoteca por su ID
    @Autowired
    private DiscotecaService discotecaService;

    @GetMapping("/eventos")
    public String showEventos(Model model) {
        model.addAttribute("eventos", eventoService.findAll());
        return "eventos";
    }

    @GetMapping("/eventos/create-event")
    public String newEventoForm(Model model) { // AÑADIDO: Pasamos el 'Model'
        // Pasamos la lista de todas las discotecas al HTML para el desplegable
        model.addAttribute("discotecas", discotecaService.findAll());
        return "create-event";
    }

    @PostMapping("/eventos/create-event")
    public String createEvento(@RequestParam String name,
                               @RequestParam Long discotecaId,       // Recibimos el ID de la discoteca
                               @RequestParam String descripcion,     // Recibimos la descripción
                               @RequestParam Integer edadRequerida,  // Recibimos la edad
                               @RequestParam MultipartFile image) throws IOException {

        // 1. Buscamos el objeto Discoteca completo usando su ID
        Discoteca discoteca = discotecaService.findById(discotecaId);

        // 2. Guardamos el evento con todos los datos
        eventoService.save(name, discoteca, descripcion, image, edadRequerida);

        return "redirect:/eventos"; // Mejor usar redirect para evitar reenvío de formularios
    }

    @GetMapping("/eventos/{id}/image")
    @ResponseBody
    public ResponseEntity<byte[]> showImage(@PathVariable long id) {
        Evento e = eventoService.findById(id);

        if (e == null || e.getImage() == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
                .body(e.getImage());
    }

    @GetMapping("/eventos/{id}/edit")
    public String editEventoForm(@PathVariable long id, Model model) {
        Evento evento = eventoService.findById(id);

        model.addAttribute("evento", evento);
        // También pasamos las discotecas a la vista de edición
        model.addAttribute("discotecas", discotecaService.findAll());

        return "edit-event";
    }

    @PostMapping("/eventos/{id}/edit")
    public String updateEvento(@PathVariable long id,
                               @RequestParam String name,
                               @RequestParam Long discotecaId,
                               @RequestParam String descripcion,
                               @RequestParam Integer edadRequerida,
                               @RequestParam(required = false) MultipartFile image) throws IOException {

        // Buscamos la discoteca
        Discoteca discoteca = discotecaService.findById(discotecaId);

        // Actualizamos
        eventoService.update(id, name, discoteca, descripcion, image, edadRequerida);

        return "redirect:/eventos";
    }

    @PostMapping("/eventos/{id}/delete")
    public String deleteEvento(@PathVariable long id) {
        eventoService.delete(id);
        return "redirect:/eventos";
    }
}