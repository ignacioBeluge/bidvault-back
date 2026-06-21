package com.bidvault.api.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "pagosVenta")
public class PagoVenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer identificador;

    @Column(nullable = false)
    private Integer registroVenta;   // FK a registroDeSubasta

    @Column(nullable = false)
    private Integer cliente;          // el comprador

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal montoPujado;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal comision;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal envio;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal montoTotal;

    @Column(nullable = false, length = 20)
    private String estado;            // PENDIENTE, PAGADO, VENCIDO

    @Column(nullable = false)
    private LocalDateTime fechaLimite;

    private LocalDateTime fechaPago;
}