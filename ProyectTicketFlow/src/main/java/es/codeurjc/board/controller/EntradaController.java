package es.codeurjc.board.controller;

import java.io.IOException;

import es.codeurjc.board.model.Evento;
import es.codeurjc.board.model.Entrada;
import es.codeurjc.board.service.EventoService;
import es.codeurjc.board.service.EntradaService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

@Controller
public class EntradaController {

    @Autowired
    private EntradaService entradaService;

    @Autowired
    private EventoService eventoService;

    @GetMapping("/eventos/{id}/entradas")
    public String showEntradas(@PathVariable Long id, Model model) {

        Evento evento = eventoService.findById(id);

        model.addAttribute("evento", evento);
        model.addAttribute("discoteca", evento.getDiscoteca());
        model.addAttribute("entradas", entradaService.findByEvento(id));

        return "entradas";
    }

    @GetMapping("/eventos/{eventoId}/entradas/create")
    public String newEntradaForm(@PathVariable Long eventoId, Model model) {

        Evento evento = eventoService.findById(eventoId);
        model.addAttribute("evento", evento);

        return "create-ticket";
    }

    @PostMapping("/entradas/create-ticket")
    public String createEntrada(@RequestParam String name,
                               @RequestParam Long eventoId,
                               @RequestParam String descripcion,
                               @RequestParam Double precio,
                               @RequestParam Integer edadRequerida,
                               @RequestParam MultipartFile image) throws IOException {

        Evento evento = eventoService.findById(eventoId);

        entradaService.save(name, evento, descripcion, precio, edadRequerida, image);

        return "redirect:/eventos/" + eventoId + "/entradas";
    }

    @GetMapping("/entradas/{id}/image")
    @ResponseBody
    public ResponseEntity<byte[]> showImage(@PathVariable long id) {

        Entrada e = entradaService.findById(id);

        if (e == null || e.getImage() == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
                .body(e.getImage());
    }

    @GetMapping("/entradas/{id}/edit")
    public String editEntradaForm(@PathVariable long id, Model model) {

        Entrada entrada = entradaService.findById(id);

        model.addAttribute("evento", entrada.getEvento());
        model.addAttribute("entrada", entrada);
        model.addAttribute("eventos", eventoService.findAll());

        return "edit-ticket";
    }

    @PostMapping("/entradas/{id}/edit")
    public String updateEntrada(@PathVariable long id,
                               @RequestParam String name,
                               @RequestParam Long eventoId,
                               @RequestParam String descripcion,
                               @RequestParam Double precio,
                               @RequestParam Integer edadRequerida,
                               @RequestParam(required = false) MultipartFile image) throws IOException {

        Evento evento = eventoService.findById(eventoId);

        entradaService.update(id, name, evento, descripcion, precio, edadRequerida, image);

        return "redirect:/eventos/" + eventoId + "/entradas";
    }

    @PostMapping("/entradas/{id}/delete")
    public String deleteEntrada(@PathVariable long id) {

        Entrada entrada = entradaService.findById(id);
        Long eventoId = entrada.getEvento().getId();

        entradaService.delete(id);

        return "redirect:/eventos/" + eventoId + "/entradas";
    }
}