package com.bidvault.api.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "productos")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer identificador;

    private LocalDate fecha;

    @Column(length = 2)
    private String disponible;

    @Column(length = 500)
    private String descripcionCatalogo;

    @Column(length = 300, nullable = false)
    private String descripcionCompleta;

    @Column(nullable = false)
    private Integer revisor;

    @Column(nullable = false)
    private Integer duenio;

    @Column(length = 30)
    private String seguro;       // nroPoliza (relación no formalizada en la BD)
}
