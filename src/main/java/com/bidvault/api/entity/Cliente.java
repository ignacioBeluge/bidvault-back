package com.bidvault.api.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "clientes")
public class Cliente {

    // OJO: clientes.identificador NO es identity, es FK hacia personas.
    // No lleva @GeneratedValue.
    @Id
    private Integer identificador;

    // Relación 1:1 con Persona. El id de cliente ES el id de persona.
    @OneToOne
    @MapsId  // indica que la PK de Cliente es también la FK hacia Persona
    @JoinColumn(name = "identificador")
    private Persona persona;

    @Column(name = "numeroPais")
    private Integer numeroPais;

    @Column(length = 2)
    private String admitido;     // 'si' / 'no'

    @Column(length = 10)
    private String categoria;    // comun, especial, plata, oro, platino

    @Column(nullable = false)
    private Integer verificador;
}
