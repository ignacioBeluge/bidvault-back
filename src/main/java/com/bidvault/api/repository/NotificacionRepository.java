package com.bidvault.api.repository;

import com.bidvault.api.entity.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificacionRepository extends JpaRepository<Notificacion, Integer> {

    // Notificaciones de una persona, las más nuevas primero
    List<Notificacion> findByPersonaOrderByFechaCreacionDesc(Integer persona);

    // Para contar las no leídas
    long countByPersonaAndLeida(Integer persona, String leida);
}