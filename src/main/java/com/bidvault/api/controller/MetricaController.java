package com.bidvault.api.controller;

import com.bidvault.api.dto.metrica.MetricasDTO;
import com.bidvault.api.security.SecurityUtils;
import com.bidvault.api.service.MetricaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/usuarios/me/metricas")
@RequiredArgsConstructor
public class MetricaController {

    private final MetricaService metricaService;

    // GET /usuarios/me/metricas
    @GetMapping
    public ResponseEntity<MetricasDTO> obtenerMetricas() {
        Integer usuarioId = SecurityUtils.getUsuarioId();
        return ResponseEntity.ok(metricaService.obtenerMetricas(usuarioId));
    }
}