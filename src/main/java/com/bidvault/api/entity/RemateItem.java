package com.bidvault.api.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "rematesItem")
public class RemateItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer identificador;

    @Column(nullable = false)
    private Integer item;              // FK a itemsCatalogo

    @Column(nullable = false, length = 2)
    private String enRemate;           // si / no

    private LocalDateTime inicioRemate;

    @Column(nullable = false, length = 2)
    private String cerrado;            // si / no

    private Integer asistenteGanador;  // null si nadie pujó

    @Column(nullable = false, length = 2)
    private String ganaEmpresa;        // si / no

    private LocalDateTime fechaCierre;
}