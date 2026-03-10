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

    @Autowired
    private DiscotecaService discotecaService;

    @GetMapping("/discotecas/{id}/eventos")
    public String showEventos(@PathVariable Long id, Model model) {

        Discoteca discoteca = discotecaService.findById(id);

        model.addAttribute("discoteca", discoteca);
        model.addAttribute("eventos", eventoService.findByDiscoteca(id));

        return "eventos";
    }

    @GetMapping("/discotecas/{id}/eventos/create")
    public String newEventoForm(@PathVariable Long id, Model model) {

        Discoteca discoteca = discotecaService.findById(id);
        model.addAttribute("discoteca", discoteca);

        return "create-event";
    }

    @PostMapping("/eventos/create-event")
    public String createEvento(@RequestParam String name,
                               @RequestParam Long discotecaId,
                               @RequestParam String descripcion,
                               @RequestParam Integer edadRequerida,
                               @RequestParam MultipartFile image) throws IOException {

        Discoteca discoteca = discotecaService.findById(discotecaId);

        eventoService.save(name, discoteca, descripcion, image, edadRequerida);

        return "redirect:/discotecas/" + discotecaId + "/eventos";
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

        Discoteca discoteca = discotecaService.findById(discotecaId);

        eventoService.update(id, name, discoteca, descripcion, image, edadRequerida);

        return "redirect:/discotecas/" + discotecaId + "/eventos";
    }

    @PostMapping("/eventos/{id}/delete")
    public String deleteEvento(@PathVariable long id) {

        Evento evento = eventoService.findById(id);
        Long discotecaId = evento.getDiscoteca().getId();

        eventoService.delete(id);

        return "redirect:/discotecas/" + discotecaId + "/eventos";
    }
}