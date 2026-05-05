package es.codeurjc.board.controller;

import es.codeurjc.board.dto.DiscotecaDTO;
import es.codeurjc.board.service.DiscotecaService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/clubs")
public class DiscotecaRestController {

    private final DiscotecaService discotecaService;
    private final es.codeurjc.board.service.FileStorageService fileStorageService;

    public DiscotecaRestController(DiscotecaService discotecaService, es.codeurjc.board.service.FileStorageService fileStorageService) {
        this.discotecaService = discotecaService;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping
    public ResponseEntity<Page<DiscotecaDTO>> getAllClubs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<DiscotecaDTO> clubs =
                discotecaService.findAllClubs(PageRequest.of(page, size));

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

        if (clubDTO.getName() == null || clubDTO.getStreet() == null || clubDTO.getDescription() == null || clubDTO.getOwnerId() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Faltan campos obligatorios. Es necesario incluir name, street, description y ownerId."));
        }

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

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createClubWithImage(
            @RequestParam String name,
            @RequestParam String street,
            @RequestParam String description,
            @RequestParam(required = false) Long ownerId,
            @RequestParam(required = false) MultipartFile imageFile,
            @RequestParam(required = false) MultipartFile flyerFile
    ) throws Exception {

        String error = discotecaService.validarCamposDiscoteca(name, street, description);
        if (error != null) {
            return ResponseEntity.badRequest().body(Map.of("error", error));
        }

        DiscotecaDTO dto = new DiscotecaDTO();
        dto.setName(name);
        dto.setStreet(street);
        dto.setDescription(description);
        dto.setOwnerId(ownerId);

        DiscotecaDTO createdClub = discotecaService.createClubWithImageAndFlyer(dto, imageFile, flyerFile);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdClub.getId())
                .toUri();

        return ResponseEntity.created(location).body(createdClub);
    }

    @PostMapping(path = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateClubWithFiles(
            @PathVariable Long id,
            @RequestParam String name,
            @RequestParam String street,
            @RequestParam String description,
            @RequestParam(required = false) Long ownerId,
            @RequestParam(required = false) Boolean removeImage,
            @RequestParam(required = false) Boolean removeFlyer,
            @RequestParam(required = false) MultipartFile imageFile,
            @RequestParam(required = false) MultipartFile flyerFile
    ) throws Exception {

        String error = discotecaService.validarCamposDiscoteca(name, street, description);
        if (error != null) {
            return ResponseEntity.badRequest().body(Map.of("error", error));
        }

        Optional<DiscotecaDTO> updatedClub = discotecaService.updateClubWithImageAndFlyer(
                id, name, street, description, ownerId, imageFile, flyerFile,
                Boolean.TRUE.equals(removeImage), Boolean.TRUE.equals(removeFlyer)
        );

        return updatedClub.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<DiscotecaDTO> updateClub(@PathVariable Long id,
                                                   @RequestBody DiscotecaDTO clubDTO) {

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

    @GetMapping("/{id}/flyer")
    public ResponseEntity<org.springframework.core.io.Resource> getClubFlyer(@PathVariable Long id) {
        java.util.Optional<DiscotecaDTO> optClub = discotecaService.findClubById(id);
        if (optClub.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        DiscotecaDTO club = optClub.get();
        if (club.getFlyerFileName() == null) {
            return ResponseEntity.notFound().build();
        }
        try {
            org.springframework.core.io.Resource resource = fileStorageService.getFileAsResource(club.getFlyerFileName());
            return ResponseEntity.ok()
                    .header(org.springframework.http.HttpHeaders.CONTENT_TYPE, "application/pdf")
                    .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}