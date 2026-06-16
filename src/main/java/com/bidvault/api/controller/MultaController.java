package com.bidvault.api.controller;

import com.bidvault.api.dto.multa.MultasResponse;
import com.bidvault.api.dto.puja.MiPujaDTO;
import com.bidvault.api.security.SecurityUtils;
import com.bidvault.api.service.MultaService;
import com.bidvault.api.service.PujaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class MultaController {

    private final MultaService multaService;
    private final PujaService pujaService;

    // GET /usuarios/me/multas
    @GetMapping("/me/multas")
    public ResponseEntity<MultasResponse> misMultas() {
        Integer usuarioId = SecurityUtils.getUsuarioId();
        return ResponseEntity.ok(multaService.misMultas(usuarioId));
    }

    // GET /usuarios/me/pujas
    @GetMapping("/me/pujas")
    public ResponseEntity<List<MiPujaDTO>> misPujas() {
        Integer usuarioId = SecurityUtils.getUsuarioId();
        return ResponseEntity.ok(pujaService.miHistorial(usuarioId));
    }
}
