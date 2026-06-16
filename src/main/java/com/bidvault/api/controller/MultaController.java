package com.bidvault.api.controller;

import com.bidvault.api.dto.multa.MultasResponse;
import com.bidvault.api.security.SecurityUtils;
import com.bidvault.api.service.MultaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class MultaController {

    private final MultaService multaService;

    // GET /usuarios/me/multas
    @GetMapping("/me/multas")
    public ResponseEntity<MultasResponse> misMultas() {
        Integer usuarioId = SecurityUtils.getUsuarioId();
        return ResponseEntity.ok(multaService.misMultas(usuarioId));
    }
}
