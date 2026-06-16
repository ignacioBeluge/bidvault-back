package com.bidvault.api.controller;

import com.bidvault.api.dto.admin.MultaAplicadaResponse;
import com.bidvault.api.service.AdminMultaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminMultaService adminMultaService;

    // POST /admin/remates/{remateItemId}/aplicar-multa
    // Aplicar multa del 10% al ganador que no presentó fondos en 72hs y bloquearlo
    @PostMapping("/remates/{remateItemId}/aplicar-multa")
    public ResponseEntity<MultaAplicadaResponse> aplicarMulta(
            @PathVariable Integer remateItemId) {
        MultaAplicadaResponse response = adminMultaService.aplicarMulta(remateItemId);
        return ResponseEntity.ok(response);
    }
}
