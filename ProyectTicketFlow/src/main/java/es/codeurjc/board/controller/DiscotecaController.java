package es.codeurjc.board.controller;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

// Importaciones de tus modelos y servicios
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
public class DiscotecaController {
    
    @Autowired
    private DiscotecaService discotecaService;

    // Inyectamos el EventoService para que el nuevo método funcione sin dar error rojo
    @Autowired
    private EventoService eventoService;

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

    @PostMapping("/discotecas/create-discotecas")
    public String createDiscoteca(@RequestParam String name,
                                  @RequestParam MultipartFile image,
                                  @RequestParam String calle,         
                                  @RequestParam String descripcion)   
                                  throws IOException {
        
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

    @PostMapping("/discotecas/{id}/delete")
    public String deleteDiscoteca(@PathVariable long id) {
        discotecaService.delete(id);
        return "redirect:/discotecas";
    }

    // --- NUEVO MÉTODO: Mostrar eventos de una discoteca reutilizando eventos.html ---
    @GetMapping("/discotecas/{id}/eventos")
    public String showEventosDeDiscoteca(@PathVariable long id, Model model) {
        
        // Buscamos todos los eventos y filtramos los que tengan la ID de esta discoteca
        List<Evento> eventosDeLaDiscoteca = eventoService.findAll().stream()
                .filter(evento -> evento.getDiscoteca() != null && evento.getDiscoteca().getId().equals(id))
                .collect(Collectors.toList());

        // Pasamos la lista filtrada a la vista "eventos"
        model.addAttribute("eventos", eventosDeLaDiscoteca);

        return "eventos"; 
    }
}