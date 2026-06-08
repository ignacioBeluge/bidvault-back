package com.bidvault.api.controller;

import com.bidvault.api.dto.puja.BidConstraintsDTO;
import com.bidvault.api.dto.puja.PuedePujarDTO;
import com.bidvault.api.dto.puja.PujaHistorialDTO;
import com.bidvault.api.dto.puja.PujaRequest;
import com.bidvault.api.dto.puja.PujaResponse;
import com.bidvault.api.security.SecurityUtils;
import com.bidvault.api.service.PujaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/auctions/{subastaId}")
@RequiredArgsConstructor
public class PujaController {

    private final PujaService pujaService;

    // GET /auctions/{subastaId}/items/{itemId}/constraints
    // Devuelve los límites del slider calculados por el servidor.
    // El front llama a esto antes de mostrar el slider de puja.
    @GetMapping("/items/{itemId}/constraints")
    public ResponseEntity<BidConstraintsDTO> obtenerLimites(
            @PathVariable Integer subastaId,
            @PathVariable Integer itemId
        ) {
        BidConstraintsDTO limites = pujaService.calcularRestricciones(subastaId, itemId);
    return ResponseEntity.ok(limites);
    }

    // POST /auctions/{subastaId}/items/{itemId}/bids
    // Realiza una puja sobre un ítem.
    @PostMapping("/items/{itemId}/bids")
    public ResponseEntity<PujaResponse> pujar(
            @PathVariable Integer subastaId,
            @PathVariable Integer itemId,
            @Valid @RequestBody PujaRequest request) {

        Integer usuarioId = SecurityUtils.getUsuarioId();
        PujaResponse response =
                pujaService.realizarPuja(subastaId, usuarioId, itemId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response); // 201
    }

    // GET /auctions/{subastaId}/can-bid
    // Indica si el usuario puede pujar en esta subasta.
    @GetMapping("/can-bid")
    public ResponseEntity<PuedePujarDTO> puedePujar(@PathVariable Integer subastaId) {
        Integer usuarioId = SecurityUtils.getUsuarioId();
        PuedePujarDTO resultado = pujaService.puedePujar(usuarioId);
        return ResponseEntity.ok(resultado);
    }

    // GET /auctions/{subastaId}/items/{itemId}/bids
    // Historial de pujas del ítem
    @GetMapping("/items/{itemId}/bids")
    public ResponseEntity<List<PujaHistorialDTO>> historial(
            @PathVariable Integer subastaId,
            @PathVariable Integer itemId) {
        Integer usuarioId = SecurityUtils.getUsuarioId();
        List<PujaHistorialDTO> historial = pujaService.obtenerHistorial(itemId, usuarioId);
        return ResponseEntity.ok(historial);
    }
}