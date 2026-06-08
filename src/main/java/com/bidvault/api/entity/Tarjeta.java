package com.bidvault.api.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "tarjetas")
public class Tarjeta {

    @Id
    private Integer medioPago;   // PK = FK a mediosDePago

    @Column(nullable = false, length = 4)
    private String ultimosCuatroDigitos;

    @Column(nullable = false, length = 15)
    private String marca;

    @Column(nullable = false, length = 150)
    private String titular;

    @Column(nullable = false, length = 5)
    private String fechaVencimiento;

    @Column(length = 2)
    private String esInternacional;

    @Column(length = 3)
    private String moneda;
}