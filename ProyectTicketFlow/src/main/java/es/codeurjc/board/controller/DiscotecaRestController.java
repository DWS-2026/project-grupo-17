package es.codeurjc.board.controller;

import es.codeurjc.board.dto.DiscotecaDTO;
import es.codeurjc.board.service.DiscotecaService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/clubs")
public class DiscotecaRestController {

    private final DiscotecaService discotecaService;

    public DiscotecaRestController(DiscotecaService discotecaService) {
        this.discotecaService = discotecaService;
    }

    @GetMapping
    public ResponseEntity<Page<DiscotecaDTO>> getAllClubs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<DiscotecaDTO> clubs = discotecaService.findAllClubs(PageRequest.of(page, size));
        return ResponseEntity.ok(clubs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DiscotecaDTO> getClubById(@PathVariable Long id) {
        return discotecaService.findClubById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createClub(@RequestBody DiscotecaDTO clubDTO) {
        //Validación de campo en una entidad en su creación
        String error = discotecaService.validarCamposDiscoteca(
                clubDTO.getName(),
                clubDTO.getStreet(),
                clubDTO.getDescription()
        );

        if (error != null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", error));
        }

        DiscotecaDTO createdClub = discotecaService.createClub(clubDTO);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdClub.getId())
                .toUri();

        return ResponseEntity.created(location).body(createdClub);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DiscotecaDTO> updateClub(@PathVariable Long id, @RequestBody DiscotecaDTO clubDTO) {
        return discotecaService.updateClub(id, clubDTO)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClub(@PathVariable Long id) {
        if (discotecaService.deleteClub(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> getClubImage(@PathVariable Long id) throws Exception {

        byte[] image = discotecaService.getClubImage(id);

        if (image == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(image);
    }

}
