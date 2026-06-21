package com.bidvault.api.controller;

import com.bidvault.api.dto.compra.CompraDTO;
import com.bidvault.api.dto.compra.PagoResponse;
import com.bidvault.api.security.SecurityUtils;
import com.bidvault.api.service.CompraService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usuarios/me/compras")
@RequiredArgsConstructor
public class CompraController {

    private final CompraService compraService;

    // GET /usuarios/me/compras
    @GetMapping
    public ResponseEntity<List<CompraDTO>> misCompras() {
        Integer usuarioId = SecurityUtils.getUsuarioId();
        return ResponseEntity.ok(compraService.misCompras(usuarioId));
    }

    // POST /usuarios/me/compras/{id}/pagar
    @PostMapping("/{id}/pagar")
    public ResponseEntity<PagoResponse> pagar(@PathVariable Integer id) {
        Integer usuarioId = SecurityUtils.getUsuarioId();
        return ResponseEntity.ok(compraService.pagar(id, usuarioId));
    }
}