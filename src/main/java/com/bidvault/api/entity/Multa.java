package com.bidvault.api.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "multas")
public class Multa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer identificador;

    @Column(nullable = false)
    private Integer cliente;

    @Column(nullable = false)
    private Integer pujo;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal importeMulta;

    @Column(nullable = false, length = 2)
    private String pagada;

    @Column(nullable = false)
    private LocalDateTime fechaAplicacion;

    private LocalDateTime fechaLimite;

    private LocalDateTime fechaPago;
}