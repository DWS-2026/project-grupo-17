package es.codeurjc.board.controller;

import java.io.IOException;
import java.sql.Blob;
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
 * Controlador de eventos:
 * permite listar, crear, editar, mostrar imagen y borrar eventos
 * dentro de una discoteca.
 */
public class EventoController {

    @Autowired
    private EventoService eventoService;

    @Autowired
    private DiscotecaService discotecaService;



    @GetMapping("/discotecas/{id}/eventos")
    // Lista los eventos de una discoteca y expone si el usuario es admin.
    public String showEventos(@PathVariable Long id, Model model, HttpServletRequest request) {

        Discoteca discoteca = discotecaService.findById(id);

        model.addAttribute("discoteca", discoteca);
        model.addAttribute("eventos", eventoService.findByDiscoteca(id));
        model.addAttribute("admin", request.isUserInRole("ADMIN"));

        return "eventos";
    }


    @GetMapping("/discotecas/{id}/eventos/create")
    // Muestra el formulario para crear evento en la discoteca seleccionada.
    public String createEventoForm(@PathVariable Long id, Model model) {

        Discoteca discoteca = discotecaService.findById(id);

        model.addAttribute("discoteca", discoteca);

        return "create-event";
    }

    @PostMapping("/discotecas/{id}/eventos/create")
    // Procesa el alta de un nuevo evento, validando datos y guardando imagen si existe.
    public String createEventoProcess(@PathVariable Long id,
                                      @ModelAttribute Evento evento,
                                      @RequestParam("imageFile") MultipartFile imageFile,
                                      Model model)
            throws IOException, SQLException {

        // 1. Obtener discoteca
        Discoteca discoteca = discotecaService.findById(id);

        if (discoteca == null) {
            return "redirect:/error-403";
        }

        String error = eventoService.validarCamposEvento(
            evento.getName(), 
            evento.getDescripcion(), 
            evento.getEdadRequerida()
        );

        if (error != null) {
            model.addAttribute("error", error);
            model.addAttribute("discoteca", discoteca);
            return "create-event";
        }

        // 2. Crear evento con imagen y guardar
        eventoService.createEventoWithImage(evento, imageFile, discoteca);

        return "redirect:/discotecas/" + id + "/eventos";
    }

    @GetMapping("/eventos/{id}/image")
    @ResponseBody
    // Devuelve la imagen del evento como bytes para renderizar en la web.
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
    // Carga formulario de edicion del evento junto con las discotecas disponibles.
    public String editEventoForm(@PathVariable long id, Model model) {

        Evento evento = eventoService.findById(id);

        model.addAttribute("discoteca", evento.getDiscoteca());
        model.addAttribute("evento", evento);
        model.addAttribute("discotecas", discotecaService.findAll());

        return "edit-event";
    }

    @PostMapping("/eventos/{id}/edit")
    // Aplica cambios en evento existente e imagen opcional.
    public String updateEventoProcess(@PathVariable long id,
                                      Evento eventoForm,
                                      @RequestParam("imageFile") MultipartFile image,                                      Model model)
            throws IOException, SQLException {

        Evento evento = eventoService.findById(id);

        if (evento == null) {
            return "redirect:/error-403";
        }

        String error = eventoService.validarCamposEvento(
            eventoForm.getName(), 
            eventoForm.getDescripcion(), 
            eventoForm.getEdadRequerida()
        );

        if (error != null) {
            model.addAttribute("error", error);
            model.addAttribute("evento", evento);
            model.addAttribute("discoteca", evento.getDiscoteca());
            model.addAttribute("discotecas", discotecaService.findAll());
            return "edit-event";
        }

        // Actualizar evento con manejo de imagen
        eventoService.updateEventoWithImage(id, eventoForm, image, eventoForm.getDiscoteca());

        return "redirect:/discotecas/" + evento.getDiscoteca().getId() + "/eventos";
    }

    @PostMapping("/eventos/{id}/delete")
    // Elimina un evento y redirige al listado de su discoteca.
    public String deleteEvento(@PathVariable long id) {

        Evento evento = eventoService.findById(id);
        Long discotecaId = evento.getDiscoteca().getId();

        eventoService.delete(id);

        return "redirect:/discotecas/" + discotecaId + "/eventos";
    }
}