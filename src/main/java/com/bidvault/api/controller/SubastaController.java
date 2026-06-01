package com.bidvault.api.controller;

import com.bidvault.api.dto.subasta.SubastaDetalleDTO;
import com.bidvault.api.dto.subasta.SubastaResumenDTO;
import com.bidvault.api.security.SecurityUtils;
import com.bidvault.api.service.SubastaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auctions")
@RequiredArgsConstructor
public class SubastaController {

    private final SubastaService subastaService;

    // GET /auctions
    // Lista las subastas que el usuario puede ver según su categoría.
    @GetMapping
    public ResponseEntity<List<SubastaResumenDTO>> listar() {
        Integer usuarioId = SecurityUtils.getUsuarioId();
        List<SubastaResumenDTO> subastas = subastaService.listarSubastas(usuarioId);
        return ResponseEntity.ok(subastas);
    }

    // GET /auctions/{id}
    // Detalle de una subasta con su catálogo.
    @GetMapping("/{id}")
    public ResponseEntity<SubastaDetalleDTO> detalle(@PathVariable Integer id) {
        Integer usuarioId = SecurityUtils.getUsuarioId();
        SubastaDetalleDTO detalle = subastaService.obtenerDetalle(id, usuarioId);
        return ResponseEntity.ok(detalle);
    }
}