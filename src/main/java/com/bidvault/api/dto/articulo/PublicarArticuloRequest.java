package com.bidvault.api.dto.articulo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class PublicarArticuloRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "La descripción es obligatoria")
    private String descripcion;

    // Las fotos en base64. La consigna pide mínimo 6.
    @NotNull(message = "Debe incluir fotos")
    private List<String> fotos;

    // Las dos declaraciones juradas (deben venir en true)
    @NotNull(message = "Debe declarar la propiedad")
    private Boolean declaraPropiedad;

    @NotNull(message = "Debe declarar el origen lícito")
    private Boolean declaraOrigenLicito;
}