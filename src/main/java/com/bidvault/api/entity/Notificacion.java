package com.bidvault.api.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "notificaciones")
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer identificador;

    @Column(nullable = false)
    private Integer persona;        // a quién va dirigida

    @Column(nullable = false, length = 30)
    private String tipo;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(nullable = false, length = 500)
    private String mensaje;

    @Column(nullable = false, length = 2)
    private String leida;           // si / no

    @Column(length = 30)
    private String destinoNavegacion;

    @Column(length = 500)
    private String parametrosNavegacion;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;
}