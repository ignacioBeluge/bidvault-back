package com.bidvault.api.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "credenciales")
public class Credencial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer identificador;

    @Column(nullable = false)
    private Integer persona;     // FK a personas

    @Column(nullable = false, length = 250, unique = true)
    private String email;

    @Column(nullable = false, length = 255)
    private String passwordHash;

    @Column(nullable = false)
    private Integer etapaRegistro;   // 1 = datos cargados, 2 = habilitado para pujar

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;
}