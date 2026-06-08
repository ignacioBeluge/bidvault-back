package com.bidvault.api.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegistroPaso1Request {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El documento es obligatorio")
    private String documento;

    @NotBlank(message = "La dirección es obligatoria")
    private String direccion;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no tiene un formato válido")
    private String email;

    // El país lo mandamos como número (FK a la tabla paises)
    private Integer numeroPais;

    // Las fotos del DNI las mandamos como base64 (string) para simplificar.
    // En producción serían multipart, pero base64 es más fácil de probar.
    private String fotoDniFrente;
    private String fotoDniDorso;
}