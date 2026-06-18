package com.bidvault.api.controller;

import com.bidvault.api.dto.notificacion.NotificacionDTO;
import com.bidvault.api.security.SecurityUtils;
import com.bidvault.api.service.NotificacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/usuarios/me/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {

    private final NotificacionService notificacionService;

    // GET /usuarios/me/notificaciones — lista todas
    @GetMapping
    public ResponseEntity<List<NotificacionDTO>> listar() {
        Integer usuarioId = SecurityUtils.getUsuarioId();
        return ResponseEntity.ok(notificacionService.listar(usuarioId));
    }

    // GET /usuarios/me/notificaciones/no-leidas — cuenta para el badge
    @GetMapping("/no-leidas")
    public ResponseEntity<Map<String, Long>> contarNoLeidas() {
        Integer usuarioId = SecurityUtils.getUsuarioId();
        long cantidad = notificacionService.contarNoLeidas(usuarioId);
        return ResponseEntity.ok(Map.of("cantidad", cantidad));
    }

    // PATCH /usuarios/me/notificaciones/{id}/leida — marca una como leída
    @PatchMapping("/{id}/leida")
    public ResponseEntity<Void> marcarLeida(@PathVariable Integer id) {
        Integer usuarioId = SecurityUtils.getUsuarioId();
        notificacionService.marcarLeida(id, usuarioId);
        return ResponseEntity.ok().build();
    }

    // PATCH /usuarios/me/notificaciones/leer-todas — marca todas
    @PatchMapping("/leer-todas")
    public ResponseEntity<Void> marcarTodasLeidas() {
        Integer usuarioId = SecurityUtils.getUsuarioId();
        notificacionService.marcarTodasLeidas(usuarioId);
        return ResponseEntity.ok().build();
    }
}