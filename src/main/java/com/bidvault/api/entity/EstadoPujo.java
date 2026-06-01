package com.bidvault.api.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "estadosPujos")
public class EstadoPujo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer identificador;

    @Column(nullable = false)
    private Integer pujo;        // FK a pujos

    @Column(nullable = false, length = 15)
    private String estado;       // pendiente, confirmada, superada, rechazada

    @Column(nullable = false)
    private LocalDateTime fechaEstado;
}