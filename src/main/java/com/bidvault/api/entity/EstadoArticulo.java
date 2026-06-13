package com.bidvault.api.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "estadosArticulo")
public class EstadoArticulo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer identificador;

    @Column(nullable = false)
    private Integer producto;          // FK a productos

    @Column(nullable = false, length = 20)
    private String estado;             // EN_REVISION, PRECIO_ASIGNADO, etc.

    @Column(precision = 18, scale = 2)
    private BigDecimal precioPropuesto;     // null hasta que la empresa lo asigna

    @Column(precision = 18, scale = 2)
    private BigDecimal comisionPropuesta;

    @Column(length = 500)
    private String motivoRechazo;

    @Column(nullable = false)
    private Integer clientePublicador;  // el cliente que subió el artículo

    @Column(nullable = false)
    private LocalDateTime fecha;
}
