package com.bidvault.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MedioPagoRequest {

    @NotBlank(message = "El tipo es obligatorio")
    private String tipo;   // cuenta_bancaria, tarjeta, cheque_certificado

    // Campos para tarjeta
    private String numeroTarjeta;
    private String marca;          // VISA, MASTERCARD, etc.
    private String titular;
    private String vencimiento;    // MM/AA
    private Boolean esInternacional;
    private String moneda;         // ARS / USD

    // Campos para cuenta bancaria
    private String banco;
    private String cbu;
    private String alias;

    // Campos para cheque
    private String bancoEmisor;
    private String numeroCheque;
    private java.math.BigDecimal monto;
}