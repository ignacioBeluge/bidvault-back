package com.bidvault.api.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "registroDeSubasta")
public class RegistroDeSubasta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer identificador;

    @Column(nullable = false)
    private Integer subasta;

    @Column(nullable = false)
    private Integer duenio;     // el dueño que vendió

    @Column(nullable = false)
    private Integer producto;

    @Column(nullable = false)
    private Integer cliente;    // el comprador (quien ganó)

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal importe;     // lo pujado

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal comision;
}
