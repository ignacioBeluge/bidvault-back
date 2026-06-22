package com.bidvault.api.entity;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "fotos")
public class Foto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer identificador;

    @Column(nullable = false)
    private Integer producto;   // FK a productos

    @Column(nullable = false)
    @JdbcTypeCode(SqlTypes.VARBINARY)
    private byte[] foto;
}