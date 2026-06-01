package com.bidvault.api.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "asistentes")
public class Asistente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer identificador;

    @Column(nullable = false)
    private Integer numeroPostor;

    @Column(nullable = false)
    private Integer cliente;     // FK a clientes

    @Column(nullable = false)
    private Integer subasta;     // FK a subastas
}