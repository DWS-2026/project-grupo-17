package es.codeurjc.board.controller;

import es.codeurjc.board.dto.EntradaDTO;
import es.codeurjc.board.service.EntradaService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/tickets")
public class EntradaRestController {

    private final EntradaService entradaService;

    public EntradaRestController(EntradaService entradaService) {
        this.entradaService = entradaService;
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
    public ResponseEntity<EntradaDTO> createTicket(@RequestBody EntradaDTO ticketDTO) {

        EntradaDTO createdTicket = entradaService.createTicket(ticketDTO);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdTicket.getId())
                .toUri();

        return ResponseEntity.created(location).body(createdTicket);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<EntradaDTO> updateTicket(@PathVariable Long id,
                                                   @RequestBody EntradaDTO ticketDTO) {

        return entradaService.updateTicket(id, ticketDTO)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTicket(@PathVariable Long id) {

        if (entradaService.deleteTicket(id)) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }
}