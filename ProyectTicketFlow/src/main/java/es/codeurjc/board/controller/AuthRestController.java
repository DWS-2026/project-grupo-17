package es.codeurjc.board.controller;

import es.codeurjc.board.dto.SignupRequestDTO;
import es.codeurjc.board.security.jwt.AuthResponse;
import es.codeurjc.board.security.jwt.LoginRequest;
import es.codeurjc.board.security.jwt.UserLoginService;
import es.codeurjc.board.service.UserService;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthRestController {

    private final UserLoginService userLoginService;
    private final UserService userService;

    public AuthRestController(UserLoginService userLoginService,
                              UserService userService) {
        this.userLoginService = userLoginService;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @RequestBody LoginRequest loginRequest,
            HttpServletResponse response) {

        return userLoginService.login(response, loginRequest);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(
            @RequestBody SignupRequestDTO request) {

        try {
            userService.registroConValidacion(
                    request.getName(),
                    request.getEmail(),
                    request.getPassword(),
                    request.getBirthDate(),
                    null
            );
            return ResponseEntity.status(201)
                    .body(Map.of("message", "Usuario registrado correctamente"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Error al registrar el usuario"));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @CookieValue(name = "RefreshToken") String refreshToken,
            HttpServletResponse response) {

        return userLoginService.refresh(response, refreshToken);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse response) {

        return ResponseEntity.ok(userLoginService.logout(response));
    }
}