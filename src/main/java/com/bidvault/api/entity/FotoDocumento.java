package com.bidvault.api.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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

    @Column(nullable = false)
    @JdbcTypeCode(SqlTypes.VARBINARY)
    private byte[] foto;

    @Column(nullable = false)
    private LocalDateTime fechaCarga;
}
