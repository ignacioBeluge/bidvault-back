package com.bidvault.api.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "catalogos")
public class Catalogo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer identificador;

    @Column(nullable = false, length = 250)
    private String descripcion;

    private Integer subasta;     // FK a subastas

    @Column(nullable = false)
    private Integer responsable; // FK a empleados
}