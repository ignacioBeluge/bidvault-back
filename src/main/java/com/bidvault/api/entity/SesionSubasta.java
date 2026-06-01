package com.bidvault.api.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "sesionesSubasta")
public class SesionSubasta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer identificador;

    @Column(nullable = false)
    private Integer cliente;     // FK a clientes

    @Column(nullable = false)
    private Integer subasta;     // FK a subastas

    @Column(nullable = false)
    private LocalDateTime fechaConexion;

    private LocalDateTime fechaDesconexion;

    @Column(nullable = false, length = 2)
    private String activa;       // 'si' / 'no' — controla 1 subasta a la vez
}
