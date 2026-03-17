package es.codeurjc.board.controller;

import java.io.IOException;
import java.sql.Blob;
import java.sql.SQLException;

import es.codeurjc.board.model.Discoteca;
import es.codeurjc.board.model.Evento;
import es.codeurjc.board.model.Image;
import es.codeurjc.board.service.DiscotecaService;
import es.codeurjc.board.service.EventoService;
import es.codeurjc.board.service.ImageService;
import es.codeurjc.board.service.UserSession;

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
    private ImageService imageService;

    @Autowired
    private DiscotecaService discotecaService;

    @Autowired
    private UserSession userSession;

    @GetMapping("/discotecas/{id}/eventos")
    public String showEventos(@PathVariable Long id, Model model) {

        Discoteca discoteca = discotecaService.findById(id);

        model.addAttribute("discoteca", discoteca);
        model.addAttribute("eventos", eventoService.findByDiscoteca(id));
        model.addAttribute("isAdmin", userSession.isAdmin());

        return "eventos";
    }

    @GetMapping("/discotecas/{id}/eventos/create")
    public String newEventoForm(@PathVariable Long id, Model model) {

        if (!userSession.isAdmin()) {
            model.addAttribute("error", "Solo los administradores pueden crear eventos");
            return "redirect:/discotecas/" + id + "/eventos";
        }

        Discoteca discoteca = discotecaService.findById(id);
        model.addAttribute("discoteca", discoteca);

        return "create-event";
    }

    @PostMapping("/discotecas/{id}/eventos/create")
    public String createEventoProcess(@PathVariable Long id,
                                      Model model,
                                      Evento evento,
                                      @RequestParam MultipartFile image) throws IOException {

        if (!userSession.isAdmin()) {
            model.addAttribute("error", "Solo los administradores pueden crear eventos");
            return "redirect:/discotecas/" + id + "/eventos";
        }

        Discoteca discoteca = discotecaService.findById(id);
        evento.setDiscoteca(discoteca);

        // Manejo de imagen
        if (!image.isEmpty()) {
            Image img = imageService.createImage(image.getInputStream());
            evento.setImage(img);
        }

        // Guardar evento
        eventoService.save(evento);

        model.addAttribute("eventoId", evento.getId());
        return "redirect:/discotecas/" + id + "/eventos";
    }

    @GetMapping("/eventos/{id}/image")
    @ResponseBody
    public ResponseEntity<byte[]> showImage(@PathVariable long id) throws IOException, SQLException {

        Evento e = eventoService.findById(id);

        if (e == null || e.getImage() == null) {
            return ResponseEntity.notFound().build();
        }

        // Obtener bytes de la entidad Image
        Blob blob = e.getImage().getImageFile();
        int blobLength = (int) blob.length();
        byte[] bytes = blob.getBytes(1, blobLength);// o e.getImage().getBytes() si adaptas ImageService

        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
                .body(bytes);
    }

    @GetMapping("/eventos/{id}/edit")
    public String editEventoForm(@PathVariable long id, Model model) {

        if (!userSession.isAdmin()) {
            Evento evento = eventoService.findById(id);
            model.addAttribute("error", "Solo los administradores pueden editar eventos");
            return "redirect:/discotecas/" + evento.getDiscoteca().getId() + "/eventos";
        }

        Evento evento = eventoService.findById(id);

        model.addAttribute("discoteca", evento.getDiscoteca()); 
        
        model.addAttribute("evento", evento);
        model.addAttribute("discotecas", discotecaService.findAll());

        return "edit-event";
    }

    @PostMapping("/eventos/{id}/edit")
    public String updateEventoProcess(@PathVariable long id,
                                      Evento eventoForm,
                                      @RequestParam(required = false) MultipartFile image,
                                      Model model) throws IOException {

        if (!userSession.isAdmin()) {
            model.addAttribute("error", "Solo los administradores pueden editar eventos");
            return "redirect:/discotecas/" + eventoForm.getDiscoteca().getId() + "/eventos";
        }

        // Obtener evento original
        Evento evento = eventoService.findById(id);

        // Actualizar campos básicos
        evento.setName(eventoForm.getName());
        evento.setDescripcion(eventoForm.getDescripcion());
        evento.setEdadRequerida(eventoForm.getEdadRequerida());

        // Actualizar discoteca si se cambió
        if (eventoForm.getDiscoteca() != null) {
            evento.setDiscoteca(eventoForm.getDiscoteca());
        }

        // Actualizar imagen si se subió nueva
        if (image != null && !image.isEmpty()) {
            Image img = imageService.createImage(image.getInputStream());
            evento.setImage(img);
        }

        eventoService.save(evento);

        return "redirect:/discotecas/" + evento.getDiscoteca().getId() + "/eventos";
    }

    @PostMapping("/eventos/{id}/delete")
    public String deleteEvento(@PathVariable long id, Model model) {

        if (!userSession.isAdmin()) {
            Evento evento = eventoService.findById(id);
            model.addAttribute("error", "Solo los administradores pueden eliminar eventos");
            return "redirect:/discotecas/" + evento.getDiscoteca().getId() + "/eventos";
        }

        Evento evento = eventoService.findById(id);
        Long discotecaId = evento.getDiscoteca().getId();

        eventoService.delete(id);

        return "redirect:/discotecas/" + discotecaId + "/eventos";
    }
}