package es.codeurjc.board.controller;

import java.io.IOException;
import java.security.Principal;
import java.sql.Blob;
import java.sql.SQLException;

import es.codeurjc.board.model.Discoteca;
import es.codeurjc.board.model.Evento;
import es.codeurjc.board.model.Image;
import es.codeurjc.board.service.DiscotecaService;
import es.codeurjc.board.service.EventoService;
import es.codeurjc.board.service.ImageService;

import jakarta.servlet.http.HttpServletRequest;

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



    @GetMapping("/discotecas/{id}/eventos")
    public String showEventos(@PathVariable Long id, Model model, HttpServletRequest request) {

        Discoteca discoteca = discotecaService.findById(id);

        model.addAttribute("discoteca", discoteca);
        model.addAttribute("eventos", eventoService.findByDiscoteca(id));
        model.addAttribute("admin", request.isUserInRole("ADMIN"));

        return "eventos";
    }


    @GetMapping("/discotecas/{id}/eventos/create")
    public String createEventoForm(@PathVariable Long id, Model model) {

        Discoteca discoteca = discotecaService.findById(id);

        model.addAttribute("discoteca", discoteca);

        return "create-event";
    }

    @PostMapping("/discotecas/{id}/eventos/create")
    public String createEventoProcess(@PathVariable Long id,
                                      @ModelAttribute Evento evento,
                                      @RequestParam("imageFile") MultipartFile imageFile,
                                      Model model)
            throws IOException, SQLException {

        // 🔥 1. Obtener discoteca
        Discoteca discoteca = discotecaService.findById(id);

        if (discoteca == null) {
            return "redirect:/error-403";
        }

        if (isBlank(evento.getName()) || isBlank(evento.getDescripcion()) || evento.getEdadRequerida() == null || evento.getEdadRequerida() < 0) {
            model.addAttribute("error", "Revisa los campos: nombre, descripcion y edad requerida");
            model.addAttribute("discoteca", discoteca);
            return "create-event";
        }

        evento.setDiscoteca(discoteca);

        // 🔥 2. Procesar imagen (manual)
        if (imageFile != null && !imageFile.isEmpty()) {

            byte[] bytes = imageFile.getBytes();
            Blob blob = new javax.sql.rowset.serial.SerialBlob(bytes);

            Image img = new Image(blob);
            evento.setImage(img);
        }

        // 🔥 3. Guardar evento
        eventoService.save(evento);

        return "redirect:/discotecas/" + id + "/eventos";
    }

    @GetMapping("/eventos/{id}/image")
    @ResponseBody
    public ResponseEntity<byte[]> showImage(@PathVariable long id) throws IOException, SQLException {

        Evento e = eventoService.findById(id);

        if (e == null || e.getImage() == null) {
            return ResponseEntity.notFound().build();
        }

        Blob blob = e.getImage().getImageFile();
        byte[] bytes = blob.getBytes(1, (int) blob.length());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
                .body(bytes);
    }

    @GetMapping("/eventos/{id}/edit")
    public String editEventoForm(@PathVariable long id, Model model) {

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
                                      Model model)
            throws IOException, SQLException {

        Evento evento = eventoService.findById(id);

        if (evento == null) {
            return "redirect:/error-403";
        }

        if (isBlank(eventoForm.getName()) || isBlank(eventoForm.getDescripcion()) || eventoForm.getEdadRequerida() == null || eventoForm.getEdadRequerida() < 0) {
            model.addAttribute("error", "Revisa los campos: nombre, descripcion y edad requerida");
            model.addAttribute("evento", evento);
            model.addAttribute("discoteca", evento.getDiscoteca());
            model.addAttribute("discotecas", discotecaService.findAll());
            return "edit-event";
        }

        // 🔹 actualizar campos normales
        evento.setName(eventoForm.getName());
        evento.setDescripcion(eventoForm.getDescripcion());
        evento.setEdadRequerida(eventoForm.getEdadRequerida());

        if (eventoForm.getDiscoteca() != null) {
            evento.setDiscoteca(eventoForm.getDiscoteca());
        }

        // 🔹 manejar imagen MANUALMENTE
        if (image != null && !image.isEmpty()) {
            byte[] bytes = image.getInputStream().readAllBytes();
            Blob blob = new javax.sql.rowset.serial.SerialBlob(bytes);
            Image img = new Image(blob);
            evento.setImage(img);
        }

        eventoService.save(evento);

        return "redirect:/discotecas/" + evento.getDiscoteca().getId() + "/eventos";
    }

    @PostMapping("/eventos/{id}/delete")
    public String deleteEvento(@PathVariable long id) {

        Evento evento = eventoService.findById(id);
        Long discotecaId = evento.getDiscoteca().getId();

        eventoService.delete(id);

        return "redirect:/discotecas/" + discotecaId + "/eventos";
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}