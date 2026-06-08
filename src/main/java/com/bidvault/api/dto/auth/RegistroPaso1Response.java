package com.bidvault.api.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RegistroPaso1Response {
    private Integer usuarioId;
    private String mensaje;
    private Integer etapaRegistro;   // queda en 1
}
