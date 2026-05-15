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

    // LISTING (only admin controlled by SecurityConfig)
    @GetMapping
    public ResponseEntity<Page<UserDTO>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<UserDTO> users = userService.findAllUsers(PageRequest.of(page, size));
        return ResponseEntity.ok(users);
    }

    // OWN PROFILE OR ADMIN
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id,
                                         Authentication auth) {

        if (!userService.canAccessUser(id, auth)) {
            return ResponseEntity.status(403).body("Access denied");
        }

        return userService.findUserById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // ADMIN ONLY
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

    // OWN PROFILE OR ADMIN
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id,
                                        @RequestBody UserDTO userDTO,
                                        Authentication auth) {

        if (!userService.canAccessUser(id, auth)) {
            return ResponseEntity.status(403)
                    .body("You cannot edit another user");
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

    // OWN PROFILE OR ADMIN
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id,
                                        Authentication auth) {

        if (!userService.canAccessUser(id, auth)) {
            return ResponseEntity.status(403).body("You cannot delete another user");
        }

        if (userService.deleteUser(id)) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }

    // OWN PROFILE OR ADMIN
    @GetMapping("/{id}/tickets")
    public ResponseEntity<?> getUserTickets(@PathVariable Long id, Authentication auth) {

        if (!userService.canAccessUser(id, auth)) {
            return ResponseEntity.status(403).body("Access denied");
        }

        return ResponseEntity.ok(entradaService.findTicketsByUser(id));
    }

    // OWN PROFILE OR ADMIN
    @GetMapping("/{id}/image")
    public ResponseEntity<?> getUserAvatar(@PathVariable Long id,
                                           Authentication auth) throws Exception {

        if (!userService.canAccessUser(id, auth)) {
            return ResponseEntity.status(403).body("You cannot view another user's image");
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

