package com.bidvault.api.dto.usuario;

import lombok.Data;

@Data
public class MedioPagoDTO {
    private Integer id;
    private String tipo;
    private String verificado;
    private String esPrincipal;
}
