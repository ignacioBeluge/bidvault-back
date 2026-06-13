package com.bidvault.api.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "declaracionesJuradas")
public class DeclaracionJurada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer identificador;

    @Column(nullable = false)
    private Integer producto;       // FK a productos

    @Column(nullable = false)
    private Integer cliente;        // FK a clientes

    @Column(nullable = false, length = 2)
    private String declaraPropiedad;     // si / no

    @Column(nullable = false, length = 2)
    private String declaraOrigenLicito;  // si / no

    @Column(nullable = false)
    private LocalDateTime fechaDeclaracion;
}