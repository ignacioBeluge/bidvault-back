package com.bidvault.api.dto.subasta;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class SubastaDetalleDTO {

    private Integer id;
    private LocalDate fecha;
    private LocalTime hora;
    private String estado;
    private String categoria;
    private String ubicacion;
    private Integer capacidadAsistentes;
    private Integer subastador;

    // Los ítems del catálogo de esta subasta.
    private List<ItemCatalogoDTO> items;
}