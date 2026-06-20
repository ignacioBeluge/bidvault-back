package com.bidvault.api.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "segurosApp")
public class Seguro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer identificador;

    @Column(nullable = false)
    private Integer producto;      // FK a productos

    // Depósito donde está la pieza
    @Column(nullable = false, length = 200)
    private String depositoNombre;

    @Column(nullable = false, length = 300)
    private String depositoDireccion;

    @Column(nullable = false, length = 100)
    private String depositoCiudad;

    @Column(length = 30)
    private String depositoTelefono;

    @Column(length = 500)
    private String depositoReferencia;

    // Datos de la póliza
    @Column(nullable = false, length = 150)
    private String aseguradora;

    @Column(nullable = false, length = 50)
    private String numeroPoliza;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal montoCobertura;

    private LocalDate vigenciaDesde;
    private LocalDate vigenciaHasta;

    @Column(length = 30)
    private String estadoPoliza;    // "Vigente", "Vencida", "Cancelada"

    @Column(length = 30)
    private String telefonoAseguradora;
}
