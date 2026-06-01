package com.bidvault.api.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data                          // Lombok: genera getters, setters, toString, etc.
@Entity                        // marca la clase como entity JPA
@Table(name = "personas")      // mapea a la tabla 'personas'

public class Persona {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // identity de SQL Server
    private Integer identificador;

    @Column(nullable = false, length = 20)
    private String documento;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(length = 250)
    private String direccion;

    @Column(length = 15)
    private String estado;       // 'activo' / 'inactivo'

    // varbinary(max) → byte[] en Java. @Lob para campos binarios grandes.
    @Lob
    private byte[] foto;
}
