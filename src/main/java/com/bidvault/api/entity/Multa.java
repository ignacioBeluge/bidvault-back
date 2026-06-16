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
    private Integer cliente;           // FK a clientes

    @Column(nullable = false)
    private Integer subasta;           // FK a subastas

    private String articulo;           // nombre del artículo (desnormalizado para display)

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal montoOfertado;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal montoMulta;     // 10% del montoOfertado

    @Column(nullable = false, length = 2)
    private String pagada;             // 'si' / 'no'

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    private LocalDateTime fechaVencimiento; // 72hs desde que ganó
}
