package com.bidvault.api.dto;

import lombok.Data;

@Data
public class MedioPagoDTO {
    private Integer id;
    private String tipo;          // cuenta_bancaria, tarjeta, cheque_certificado
    private String verificado;    // si / no
    private String esPrincipal;   // si / no
}