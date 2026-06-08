package com.bidvault.api.controller;

import com.bidvault.api.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.bidvault.api.dto.auth.*;
import com.bidvault.api.service.RegistroService;

@RestController
@RequestMapping("/auth")     // todos los endpoints arrancan con /auth
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RegistroService registroService;


    // POST /auth/login
    // @Valid activa las validaciones del DTO (si fallan → 400 automático)
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);   // 200 con el token
    }

    // POST /auth/registro/paso1
    @PostMapping("/registro/paso1")
    public ResponseEntity<RegistroPaso1Response> registroPaso1(
            @Valid @RequestBody RegistroPaso1Request request) {
        RegistroPaso1Response response = registroService.registrarPaso1(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // POST /auth/registro/paso2
    @PostMapping("/registro/paso2")
    public ResponseEntity<LoginResponse> registroPaso2(
            @Valid @RequestBody RegistroPaso2Request request) {
        LoginResponse response = registroService.registrarPaso2(request);
        return ResponseEntity.ok(response);
    }

    // GET /auth/registro/estado/{usuarioId}
    @GetMapping("/registro/estado/{usuarioId}")
    public ResponseEntity<RegistroEstadoDTO> consultarEstado(@PathVariable Integer usuarioId) {
        return ResponseEntity.ok(registroService.consultarEstado(usuarioId));
    }
}
