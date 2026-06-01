package com.bidvault.api.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "itemsCatalogo")
public class ItemCatalogo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer identificador;

    @Column(nullable = false)
    private Integer catalogo;    // FK a catalogos

    @Column(nullable = false)
    private Integer producto;    // FK a productos

    // decimal(18,2) → BigDecimal en Java (nunca double para plata)
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal precioBase;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal comision;

    @Column(length = 2)
    private String subastado;    // 'si' / 'no'
}
