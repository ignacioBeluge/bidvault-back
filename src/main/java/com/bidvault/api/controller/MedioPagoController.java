package com.bidvault.api.controller;

import com.bidvault.api.dto.MedioPagoDTO;
import com.bidvault.api.dto.MedioPagoRequest;
import com.bidvault.api.security.SecurityUtils;
import com.bidvault.api.service.MedioPagoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class MedioPagoController {

    private final MedioPagoService medioPagoService;

    // GET /usuarios/me/medios-pago
    // Lista los medios de pago del usuario autenticado
    @GetMapping("/me/medios-pago")
    public ResponseEntity<List<MedioPagoDTO>> misMediosDePago() {
        Integer usuarioId = SecurityUtils.getUsuarioId();
        List<MedioPagoDTO> medios = medioPagoService.listarPorCliente(usuarioId);
        return ResponseEntity.ok(medios);
    }

    // POST /usuarios/me/medios-pago
    @PostMapping("/me/medios-pago")
    public ResponseEntity<MedioPagoDTO> crear(@Valid @RequestBody MedioPagoRequest request) {
        Integer usuarioId = SecurityUtils.getUsuarioId();
        MedioPagoDTO creado = medioPagoService.crear(usuarioId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    // DELETE /usuarios/me/medios-pago/{id}
    @DeleteMapping("/me/medios-pago/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        Integer usuarioId = SecurityUtils.getUsuarioId();
        medioPagoService.eliminar(id, usuarioId);
        return ResponseEntity.ok().build();
    }
}
