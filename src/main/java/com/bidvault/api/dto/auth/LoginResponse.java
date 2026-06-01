package com.bidvault.api.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor   // constructor con todos los campos
public class LoginResponse {

    private String token;          // el JWT que usará el cliente
    private Integer usuarioId;
    private String nombre;
    private String categoria;      // comun, especial, plata, oro, platino
    private Integer etapaRegistro; // 1 o 2 (si puede pujar o no)
}