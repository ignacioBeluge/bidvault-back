package com.bidvault.api.dto.articulo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AceptarCondicionesRequest {

    @NotNull(message = "Debe indicar si acepta o no")
    private Boolean acepta;   // true = acepta el precio, false = lo rechaza
}
