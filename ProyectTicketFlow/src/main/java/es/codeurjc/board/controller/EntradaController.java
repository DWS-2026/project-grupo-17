package es.codeurjc.board.controller;

import es.codeurjc.board.model.Evento;
import es.codeurjc.board.model.Entrada;
import es.codeurjc.board.service.EventoService;
import jakarta.servlet.http.HttpServletRequest;
import es.codeurjc.board.service.EntradaService;

import es.codeurjc.board.model.User;
import es.codeurjc.board.service.UserService;

import java.util.Collection;
import java.util.ArrayList;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class EntradaController {

    @Autowired
    private UserService userService;

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
                                @RequestParam Long eventoId,
                                Model model) {

        Evento evento = eventoService.findById(eventoId);

        if (evento == null) {
            return "redirect:/error-403";
        }

        if (isBlank(name) || isBlank(acceso) || isBlank(incluye) || precio == null || precio < 0) {
            model.addAttribute("error", "Revisa los campos obligatorios y el precio");
            model.addAttribute("evento", evento);
            return "create-ticket";
        }

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
                                @RequestParam Double precio,
                                Model model) {

        Entrada entrada = entradaService.findById(id);

        if (entrada == null) {
            return "redirect:/error-403";
        }

        Evento evento = entrada.getEvento();

        if (isBlank(name) || isBlank(acceso) || isBlank(incluye) || precio == null || precio < 0) {
            model.addAttribute("error", "Revisa los campos obligatorios y el precio");
            model.addAttribute("entrada", entrada);
            model.addAttribute("evento", evento);
            return "edit-ticket";
        }

        entradaService.update(id, name, acceso, incluye, precio, evento);

        return "redirect:/eventos/" + evento.getId() + "/entradas";
    }


    @PostMapping("/entradas/{id}/delete")
    public String deleteEntrada(@PathVariable long id) {

        Entrada entrada = entradaService.findById(id);

        if (entrada != null) {

            Collection<User> users = userService.findAll();

            for (User user : users) {
                if (user.getEntradasCompradas() != null) {
                    user.getEntradasCompradas().remove(entrada);
                    userService.saveUser(user);
                }
            }


            entradaService.delete(id);
        }

        return "redirect:/eventos/" + entrada.getEvento().getId() + "/entradas";
    }

    @GetMapping("/entradas/{id}/pago")
    public String comprarEntrada(@PathVariable Long id, Principal principal, Model model) {

        if (principal == null || principal.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }

        Entrada entrada = entradaService.findById(id);

        if (entrada == null) {
            return "redirect:/";
        }

        User user = userService.findByEmail(principal.getName()).orElse(null);

        if (user != null) {

            List<Entrada> entradas = user.getEntradasCompradas();

            if (entradas == null) {
                entradas = new ArrayList<>();
            }

            boolean yaComprada = entradas.stream()
                    .anyMatch(e -> e.getId().equals(entrada.getId()));

            if (yaComprada) {
                // MENSAJE
                model.addAttribute("error", "Ya has comprado esta entrada");

                // volver a la misma vista de entradas
                model.addAttribute("evento", entrada.getEvento());
                model.addAttribute("discoteca", entrada.getEvento().getDiscoteca());
                model.addAttribute("entradas", entradaService.findByEvento(entrada.getEvento().getId()));

                return "entradas";
            }

            entradas.add(entrada);
            user.setEntradasCompradas(entradas);
            userService.saveUser(user);
        }

        return "redirect:/profile";
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}