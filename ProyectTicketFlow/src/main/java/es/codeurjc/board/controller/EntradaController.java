package es.codeurjc.board.controller;

import es.codeurjc.board.model.Evento;
import es.codeurjc.board.model.Entrada;
import es.codeurjc.board.service.EventoService;
import jakarta.servlet.http.HttpServletRequest;
import es.codeurjc.board.service.EntradaService;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class EntradaController {

    @Autowired
    private EntradaService entradaService;

    @Autowired
    private EventoService eventoService;

    @ModelAttribute
	public void addAttributes(Model model, HttpServletRequest request) {

		Principal principal = request.getUserPrincipal();

		if (principal != null) {

			model.addAttribute("logged", true);
			model.addAttribute("email", principal.getName());
			model.addAttribute("admin", request.isUserInRole("ADMIN"));

		} else {
			model.addAttribute("logged", false);
		}
	}


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
                                @RequestParam String acceso,
                                @RequestParam String incluye,
                                @RequestParam Double precio,
                                @RequestParam Long eventoId) {

        Evento evento = eventoService.findById(eventoId);

        entradaService.save(name, acceso, incluye, precio, evento);

        return "redirect:/eventos/" + eventoId + "/entradas";
    }


    @GetMapping("/entradas/{id}/edit")
    public String editEntradaForm(@PathVariable long id, Model model) {

        Entrada entrada = entradaService.findById(id);

        model.addAttribute("entrada", entrada);
        model.addAttribute("evento", entrada.getEvento());

        return "edit-ticket";
    }


    @PostMapping("/entradas/{id}/edit")
    public String updateEntrada(@PathVariable long id,
                                @RequestParam String name,
                                @RequestParam String acceso,
                                @RequestParam String incluye,
                                @RequestParam Double precio) {

        Entrada entrada = entradaService.findById(id);
        Evento evento = entrada.getEvento();

        entradaService.update(id, name, acceso, incluye, precio, evento);

        return "redirect:/eventos/" + evento.getId() + "/entradas";
    }


    @PostMapping("/entradas/{id}/delete")
    public String deleteEntrada(@PathVariable long id) {

        Entrada entrada = entradaService.findById(id);
        Long eventoId = entrada.getEvento().getId();

        entradaService.delete(id);

        return "redirect:/eventos/" + eventoId + "/entradas";
    }
}