package com.bidvault.api.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "cuentasBancarias")
public class CuentaBancaria {

    @Id
    private Integer medioPago;   // PK = FK a mediosDePago

    @Column(nullable = false, length = 150)
    private String banco;

    @Column(nullable = false, length = 50)
    private String numeroCuenta;

    @Column(nullable = false, length = 22)
    private String cbu;

    @Column(length = 100)
    private String alias;

    @Column(nullable = false, length = 150)
    private String titular;

    @Column(length = 2)
    private String esExtranjera;

    @Column(length = 3)
    private String moneda;
}