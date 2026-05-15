package es.codeurjc.board.controller;

import es.codeurjc.board.model.Evento;
import es.codeurjc.board.model.Entrada;
import es.codeurjc.board.service.EventoService;
import jakarta.servlet.http.HttpServletRequest;
import es.codeurjc.board.service.EntradaService;

import es.codeurjc.board.model.User;
import es.codeurjc.board.service.UserService;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
/**
 * Ticket controller:
 * manages the listing of tickets for an event, their creation/editing/deletion
 * and the purchase of tickets by authenticated users.
 */
public class EntradaController {

    @Autowired
    private UserService userService;

    @Autowired
    private EntradaService entradaService;

    @Autowired
    private EventoService eventoService;

    @ModelAttribute
	// This method runs before each handler to expose common session data in views.
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
    // Displays all tickets associated with a specific event.
    public String showEntradas(@PathVariable Long id, Model model) {

        Evento evento = eventoService.findById(id);

        model.addAttribute("evento", evento);
        model.addAttribute("discoteca", evento.getDiscoteca());
        model.addAttribute("entradas", entradaService.findByEvento(id));

        return "entradas";
    }


    @GetMapping("/eventos/{eventoId}/entradas/create")
    // Loads the ticket creation form for an event.
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
                                @RequestParam Long eventoId,
                                Model model) {

        Evento evento = eventoService.findById(eventoId);

        if (evento == null) {
            return "redirect:/error-403";
        }

        try {
            entradaService.crearEntrada(name, acceso, incluye, precio, eventoId);
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("evento", evento);
            return "create-ticket";
        }

        return "redirect:/eventos/" + eventoId + "/entradas";
    }


    @GetMapping("/entradas/{id}/edit")
    // Loads the edit view with the current ticket data.
    public String editEntradaForm(@PathVariable long id, Model model) {

        Entrada entrada = entradaService.findById(id);

        if (entrada == null) {
            return "redirect:/error-403";
        }

        model.addAttribute("entrada", entrada);
        model.addAttribute("evento", entrada.getEvento());

        return "edit-ticket";
    }

    @PostMapping("/entradas/{id}/edit")
    public String updateEntrada(@PathVariable long id,
                                @RequestParam String name,
                                @RequestParam String acceso,
                                @RequestParam String incluye,
                                @RequestParam Double precio,
                                Model model) {

        Entrada entrada = entradaService.findById(id);

        if (entrada == null) {
            return "redirect:/error-403";
        }

        Evento evento = entrada.getEvento();

        try {
            entradaService.actualizarEntrada(id, name, acceso, incluye, precio, evento.getId());
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("entrada", entrada);
            model.addAttribute("evento", evento);
            return "edit-ticket";
        }

        return "redirect:/eventos/" + evento.getId() + "/entradas";
    }


    @PostMapping("/entradas/{id}/delete")
    // Deletes the ticket and removes it from any users who had purchased it.
    public String deleteEntrada(@PathVariable long id) {

        Entrada entrada = entradaService.findById(id);

        if (entrada != null) {
            entradaService.deleteEntradaConLimpieza(id);
            return "redirect:/eventos/" + entrada.getEvento().getId() + "/entradas";
        }

        return "redirect:/eventos";
    }

    @PostMapping("/entradas/{id}/pago")
    public String comprarEntrada(@PathVariable Long id, Principal principal, Model model) {

        if (principal == null || principal.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }

        Entrada entrada = entradaService.findById(id);

        if (entrada == null) {
            return "redirect:/";
        }

        User user = userService.findByEmail(principal.getName()).orElse(null);

        String resultado = entradaService.comprarEntrada(id, user);

        if ("error_ya_comprada".equals(resultado)) {
            model.addAttribute("error", "You have already purchased this ticket");
            model.addAttribute("evento", entrada.getEvento());
            model.addAttribute("discoteca", entrada.getEvento().getDiscoteca());
            model.addAttribute("entradas", entradaService.findByEvento(entrada.getEvento().getId()));
            return "entradas";
        }

        if ("success".equals(resultado)) {
            return "redirect:/profile";
        }

        return "redirect:/";
    }
}