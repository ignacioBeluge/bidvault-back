package com.bidvault.api.dto.notificacion;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NotificacionDTO {
    private Integer id;
    private String tipo;
    private String titulo;
    private String mensaje;
    private boolean leida;
    private String destinoNavegacion;
    private String parametrosNavegacion;
    private LocalDateTime fechaCreacion;
}