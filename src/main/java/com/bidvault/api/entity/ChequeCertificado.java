package com.bidvault.api.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Data
@Entity
@Table(name = "chequesCertificados")
public class ChequeCertificado {

    @Id
    private Integer medioPago;

    @Column(nullable = false, length = 150)
    private String bancoEmisor;

    @Column(nullable = false, length = 30)
    private String numeroCheque;

    @Column(nullable = false)
    private LocalDate fechaEmision;

    @Column(nullable = false)
    private LocalDate fechaVencimiento;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal monto;

    @JdbcTypeCode(SqlTypes.VARBINARY)
    private byte[] foto;
}