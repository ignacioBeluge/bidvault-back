package com.bidvault.api.dto.subasta;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class SubastaResumenDTO {

    private Integer id;
    private LocalDate fecha;
    private LocalTime hora;
    private String estado;
    private String categoria;
    private String ubicacion;
}
