package com.bidvault.api.controller;

import com.bidvault.api.dto.articulo.*;
import com.bidvault.api.dto.seguro.AmpliarPolizaResponse;
import com.bidvault.api.dto.seguro.SeguroDTO;
import com.bidvault.api.security.SecurityUtils;
import com.bidvault.api.service.ArticuloService;
import com.bidvault.api.service.SeguroService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/articulos")
@RequiredArgsConstructor
public class ArticuloController {

    private final ArticuloService articuloService;
    private final SeguroService seguroService;

    // POST /articulos
    // Publicar un artículo nuevo (queda EN_REVISION)
    @PostMapping
    public ResponseEntity<ArticuloResponse> publicar(
            @Valid @RequestBody PublicarArticuloRequest request) {
        Integer usuarioId = SecurityUtils.getUsuarioId();
        ArticuloResponse response = articuloService.publicar(usuarioId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // GET /articulos
    // Listar mis artículos
    @GetMapping
    public ResponseEntity<List<ArticuloDTO>> misArticulos() {
        Integer usuarioId = SecurityUtils.getUsuarioId();
        List<ArticuloDTO> articulos = articuloService.misArticulos(usuarioId);
        return ResponseEntity.ok(articulos);
    }

    // GET /articulos/{id}
    // Detalle de un artículo con todas las fotos
    @GetMapping("/{id}")
    public ResponseEntity<ArticuloDetalleDTO> detalle(@PathVariable Integer id) {
        Integer usuarioId = SecurityUtils.getUsuarioId();
        ArticuloDetalleDTO detalle = articuloService.obtenerDetalle(id, usuarioId);
        return ResponseEntity.ok(detalle);
    }

    // POST /articulos/{id}/responder
    // Aceptar o rechazar el precio propuesto por la empresa
    @PostMapping("/{id}/responder")
    public ResponseEntity<ArticuloResponse> responderCondiciones(
            @PathVariable Integer id,
            @Valid @RequestBody AceptarCondicionesRequest request) {
        Integer usuarioId = SecurityUtils.getUsuarioId();
        ArticuloResponse response =
                articuloService.responderCondiciones(id, usuarioId, request);
        return ResponseEntity.ok(response);
    }

    // GET /articulos/{id}/seguro
    // Ver datos del seguro y depósito de un artículo propio
    @GetMapping("/{id}/seguro")
    public ResponseEntity<SeguroDTO> verSeguro(@PathVariable Integer id) {
        Integer usuarioId = SecurityUtils.getUsuarioId();
        SeguroDTO seguro = seguroService.obtenerSeguro(id, usuarioId);
        return ResponseEntity.ok(seguro);
    }

    // POST /articulos/{id}/seguro/ampliar
    // Solicitar ampliación de la póliza del artículo
    @PostMapping("/{id}/seguro/ampliar")
    public ResponseEntity<AmpliarPolizaResponse> ampliarSeguro(@PathVariable Integer id) {
        Integer usuarioId = SecurityUtils.getUsuarioId();
        AmpliarPolizaResponse response = seguroService.ampliarPoliza(id, usuarioId);
        return ResponseEntity.ok(response);
    }
}