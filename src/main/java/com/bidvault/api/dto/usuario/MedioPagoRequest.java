package com.bidvault.api.dto.usuario;

import lombok.Data;

@Data
public class MedioPagoRequest {
    private String tipo;          // tarjeta, cuenta_bancaria, cheque_certificado
    // Tarjeta
    private String numeroTarjeta;
    private String marca;
    private String titular;
    private String vencimiento;
    // Cuenta bancaria
    private String cbu;
    private String banco;
    private String tipoCuenta;
    // Cheque
    private String numeroCheque;
    private String fechaVencimiento;
    // Común
    private Boolean esInternacional;
    private String moneda;
}
