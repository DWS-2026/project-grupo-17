package es.codeurjc.board.controller;

import es.codeurjc.board.dto.UserDTO;
import es.codeurjc.board.service.EntradaService;
import es.codeurjc.board.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/users")
public class UserRestController {

    private final UserService userService;
    private final EntradaService entradaService;

    public UserRestController(UserService userService, EntradaService entradaService) {
        this.userService = userService;
        this.entradaService = entradaService;
    }

    // LISTADO (solo admin lo controla SecurityConfig)
    @GetMapping
    public ResponseEntity<Page<UserDTO>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<UserDTO> users = userService.findAllUsers(PageRequest.of(page, size));
        return ResponseEntity.ok(users);
    }

    // PERFIL PROPIO O ADMIN
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id,
                                         Authentication auth) {

        if (!userService.canAccessUser(id, auth)) {
            return ResponseEntity.status(403).body("Acceso denegado");
        }

        return userService.findUserById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // SOLO ADMIN
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody UserDTO userDTO) {

        try {

            UserDTO createdUser = userService.createUser(userDTO);

            URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(createdUser.getId())
                    .toUri();

            return ResponseEntity
                    .created(location)
                    .body(createdUser);

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest()
                    .body(java.util.Map.of("error", e.getMessage()));
        }
    }

    // PERFIL PROPIO O ADMIN
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id,
                                        @RequestBody UserDTO userDTO,
                                        Authentication auth) {

        if (!userService.canAccessUser(id, auth)) {
            return ResponseEntity.status(403)
                    .body("No puedes editar otro usuario");
        }

        try {

            return userService.updateUser(id, userDTO)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest()
                    .body(java.util.Map.of("error", e.getMessage()));
        }
    }

    // PERFIL PROPIO O ADMIN
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id,
                                        Authentication auth) {

        if (!userService.canAccessUser(id, auth)) {
            return ResponseEntity.status(403).body("No puedes borrar otro usuario");
        }

        if (userService.deleteUser(id)) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }

    // PERFIL PROPIO O ADMIN
    @GetMapping("/{id}/tickets")
    public ResponseEntity<?> getUserTickets(@PathVariable Long id, Authentication auth) {

        if (!userService.canAccessUser(id, auth)) {
            return ResponseEntity.status(403).body("Acceso denegado");
        }

        return ResponseEntity.ok(entradaService.findTicketsByUser(id));
    }

    // PERFIL PROPIO O ADMIN
    @GetMapping("/{id}/image")
    public ResponseEntity<?> getUserAvatar(@PathVariable Long id,
                                           Authentication auth) throws Exception {

        if (!userService.canAccessUser(id, auth)) {
            return ResponseEntity.status(403).body("No puedes ver otra imagen");
        }

        byte[] image = userService.getUserAvatar(id);

        if (image == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(image);
    }
}

