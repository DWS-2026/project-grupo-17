package es.codeurjc.board.controller;

import java.io.IOException;
import java.sql.SQLException;

import es.codeurjc.board.model.Discoteca;
import es.codeurjc.board.model.Evento;
import es.codeurjc.board.service.DiscotecaService;
import es.codeurjc.board.service.EventoService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

@Controller
/**
 * Event controller:
 * allows listing, creating, editing, displaying images and deleting events
 * within a club.
 */
public class EventoController {

    @Autowired
    private EventoService eventoService;

    @Autowired
    private DiscotecaService discotecaService;



    @GetMapping("/discotecas/{id}/eventos")
    // Lists the events of a club and exposes whether the user is an admin.
    public String showEventos(@PathVariable Long id, Model model, HttpServletRequest request) {

        Discoteca discoteca = discotecaService.findById(id);

        model.addAttribute("discoteca", discoteca);
        model.addAttribute("eventos", eventoService.findByDiscoteca(id));
        model.addAttribute("admin", request.isUserInRole("ADMIN"));

        return "eventos";
    }


    @GetMapping("/discotecas/{id}/eventos/create")
    // Displays the form to create an event in the selected club.
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

        Discoteca discoteca = discotecaService.findById(id);

        if (discoteca == null) {
            return "redirect:/error-403";
        }

        try {

            eventoService.crearEvento(
                    evento.getName(),
                    evento.getDescripcion(),
                    evento.getEdadRequerida(),
                    discoteca.getId(),
                    imageFile
            );

            return "redirect:/discotecas/" + id + "/eventos";

        } catch (IllegalArgumentException e) {

            model.addAttribute("error", e.getMessage());
            model.addAttribute("discoteca", discoteca);

            return "create-event";
        }
    }

    @GetMapping("/eventos/{id}/image")
    @ResponseBody
    // Returns the event image as bytes for rendering on the web.
    public ResponseEntity<byte[]> showImage(@PathVariable long id) throws IOException, SQLException {

        Evento e = eventoService.findById(id);

        if (e == null || e.getImage() == null) {
            return ResponseEntity.notFound().build();
        }

        // Uses the service method that supports reading from disk or DB
        byte[] bytes = eventoService.getEventImage(id);
        
        if (bytes == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
                .body(bytes);
    }

    @GetMapping("/eventos/{id}/edit")
    // Loads the event edit form along with the available clubs.
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
                                      @RequestParam("imageFile") MultipartFile image,
                                      Model model)
            throws IOException, SQLException {

        Evento evento = eventoService.findById(id);

        if (evento == null) {
            return "redirect:/error-403";
        }

        try {

            eventoService.actualizarEvento(
                    id,
                    eventoForm.getName(),
                    eventoForm.getDescripcion(),
                    eventoForm.getEdadRequerida(),
                    eventoForm.getDiscoteca() != null
                            ? eventoForm.getDiscoteca().getId()
                            : evento.getDiscoteca().getId(),
                    image
            );

            return "redirect:/discotecas/" +
                    evento.getDiscoteca().getId() +
                    "/eventos";

        } catch (IllegalArgumentException e) {

            model.addAttribute("error", e.getMessage());
            model.addAttribute("evento", evento);
            model.addAttribute("discoteca", evento.getDiscoteca());
            model.addAttribute("discotecas", discotecaService.findAll());

            return "edit-event";
        }
    }

    @PostMapping("/eventos/{id}/delete")
    // Deletes an event and redirects to its club's listing.
    public String deleteEvento(@PathVariable long id) {

        Evento evento = eventoService.findById(id);
        Long discotecaId = evento.getDiscoteca().getId();

        eventoService.delete(id);

        return "redirect:/discotecas/" + discotecaId + "/eventos";
    }
}