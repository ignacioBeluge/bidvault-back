package com.bidvault.api.controller;

import com.bidvault.api.dto.auth.LoginRequest;
import com.bidvault.api.dto.auth.LoginResponse;
import com.bidvault.api.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")     // todos los endpoints arrancan con /auth
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // POST /auth/login
    // @Valid activa las validaciones del DTO (si fallan → 400 automático)
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);   // 200 con el token
    }
}
