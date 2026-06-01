package com.bidvault.api.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "pujos")
public class Pujo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer identificador;

    @Column(nullable = false)
    private Integer asistente;   // FK a asistentes

    @Column(nullable = false)
    private Integer item;        // FK a itemsCatalogo

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal importe;

    @Column(length = 2)
    private String ganador;      // 'si' / 'no', default 'no'
}
