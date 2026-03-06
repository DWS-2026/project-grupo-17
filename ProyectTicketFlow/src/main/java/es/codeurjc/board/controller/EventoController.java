package es.codeurjc.board.controller;

import java.io.IOException;

import es.codeurjc.board.model.Evento;
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

    @GetMapping("/eventos")
    public String showEventos(Model model) {

        model.addAttribute("eventos", eventoService.findAll());

        return "eventos";
    }

    @GetMapping("/eventos/create-event")
    public String newEventoForm() {

        return "create-event";
    }

    @PostMapping("/eventos/create-event")
    public String createEvento(@RequestParam String name,
                               @RequestParam MultipartFile image) throws IOException {

        eventoService.save(name, image);

        return "eventos";
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

        return "edit-event";
    }

    @PostMapping("/eventos/{id}/edit")
    public String updateEvento(@PathVariable long id,
                               @RequestParam String name,
                               @RequestParam(required = false) MultipartFile image) throws IOException {

        eventoService.update(id, name, image);

        return "eventos";
    }

    @PostMapping("/eventos/{id}/delete")
    public String deleteEvento(@PathVariable long id) {

        eventoService.delete(id);

        return "eventos";
    }
}