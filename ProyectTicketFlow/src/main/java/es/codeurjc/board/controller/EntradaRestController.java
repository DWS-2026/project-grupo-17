package es.codeurjc.board.controller;

import es.codeurjc.board.dto.EntradaDTO;
import es.codeurjc.board.model.User;
import es.codeurjc.board.service.EntradaService;
import es.codeurjc.board.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/tickets")
public class EntradaRestController {

    private final EntradaService entradaService;
    private final UserService userService;

    public EntradaRestController(EntradaService entradaService, UserService userService) {
        this.entradaService = entradaService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<Page<EntradaDTO>> getAllTickets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<EntradaDTO> tickets = entradaService.findAllTickets(PageRequest.of(page, size));
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntradaDTO> getTicketById(@PathVariable Long id) {
        return entradaService.findTicketById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createTicket(@RequestBody EntradaDTO ticketDTO) {

        try {
            EntradaDTO createdTicket = entradaService.createTicket(ticketDTO);

            URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(createdTicket.getId())
                    .toUri();

            return ResponseEntity.created(location).body(createdTicket);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTicket(@PathVariable Long id,
                                          @RequestBody EntradaDTO ticketDTO) {

        try {
            return entradaService.updateTicket(id, ticketDTO)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTicket(@PathVariable Long id) {

        if (entradaService.deleteTicket(id)) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/purchase")
    public ResponseEntity<?> purchaseTicket(@PathVariable Long id, Authentication auth) {

        if (auth == null || auth.getName() == null) {
            return ResponseEntity.status(401).body(java.util.Map.of("error", "You must be authenticated to purchase a ticket"));
        }

        User user = userService.findByEmail(auth.getName()).orElse(null);

        if (user == null) {
            return ResponseEntity.status(404).body(java.util.Map.of("error", "User not found"));
        }

        if (user.getRoles() != null && user.getRoles().contains("ADMIN")) {
            return ResponseEntity.status(403).body(java.util.Map.of("error", "Admins cannot purchase tickets"));
        }

        String resultado = entradaService.comprarEntrada(id, user);

        switch (resultado) {
            case "success":
                return ResponseEntity.ok(java.util.Map.of(
                    "message", "Ticket purchased successfully",
                    "ticketId", id
                ));
            case "error_ya_comprada":
                return ResponseEntity.status(409).body(java.util.Map.of("error", "You have already purchased this ticket"));
            case "error_entrada_no_existe":
                return ResponseEntity.status(404).body(java.util.Map.of("error", "The ticket does not exist"));
            case "error_usuario_no_existe":
                return ResponseEntity.status(404).body(java.util.Map.of("error", "User not found"));
            default:
                return ResponseEntity.status(500).body(java.util.Map.of("error", "Error processing the purchase"));
        }
    }
}
