package com.bidvault.api.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "duenios")
public class Duenio {

    // Igual que Cliente: la PK es también FK a personas (no es identity)
    @Id
    private Integer identificador;

    @Column(name = "numeroPais")
    private Integer numeroPais;

    // OJO: en la base estas columnas tienen tilde (verificaciónFinanciera).
    // Usamos @Column con el nombre exacto para que Hibernate las encuentre.
    @Column(name = "verificaciónFinanciera", length = 2)
    private String verificacionFinanciera;

    @Column(name = "verificaciónJudicial", length = 2)
    private String verificacionJudicial;

    @Column(name = "calificacionRiesgo")
    private Integer calificacionRiesgo;

    @Column(nullable = false)
    private Integer verificador;
}