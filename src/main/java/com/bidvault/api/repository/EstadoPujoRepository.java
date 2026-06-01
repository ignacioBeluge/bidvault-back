package com.bidvault.api.repository;

import com.bidvault.api.entity.EstadoPujo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EstadoPujoRepository extends JpaRepository<EstadoPujo, Integer> {

    // Trae el historial de estados de una puja ordenado por fecha desc.
    // El primero es el estado actual.
    List<EstadoPujo> findByPujoOrderByFechaEstadoDesc(Integer pujo);
}
