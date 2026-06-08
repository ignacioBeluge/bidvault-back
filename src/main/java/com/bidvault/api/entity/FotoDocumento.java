package com.bidvault.api.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "fotosDocumento")
public class FotoDocumento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer identificador;

    @Column(nullable = false)
    private Integer persona;

    @Column(nullable = false, length = 6)
    private String tipo;        // 'frente' / 'dorso'

    @Lob
    @Column(nullable = false)
    private byte[] foto;

    @Column(nullable = false)
    private LocalDateTime fechaCarga;
}
