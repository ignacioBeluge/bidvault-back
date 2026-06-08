package com.bidvault.api.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RegistroEstadoDTO {
    private Integer usuarioId;
    private Integer etapaRegistro;
    private boolean aprobado;
    private String categoria;
}