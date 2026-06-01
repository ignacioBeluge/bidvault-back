package com.bidvault.api.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "mediosDePago")
public class MedioDePago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer identificador;

    @Column(nullable = false)
    private Integer cliente;     // FK a clientes

    @Column(nullable = false, length = 20)
    private String tipo;         // cuenta_bancaria, tarjeta, cheque_certificado

    @Column(length = 2)
    private String esPrincipal;

    @Column(nullable = false, length = 2)
    private String verificado;   // 'si' / 'no' — clave para poder pujar

    @Column(nullable = false)
    private LocalDateTime fechaRegistro;
}
