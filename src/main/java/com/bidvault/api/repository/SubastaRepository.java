package com.bidvault.api.repository;

import com.bidvault.api.entity.Subasta;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SubastaRepository extends JpaRepository<Subasta, Integer> {

    // Lista las subastas por estado (ej: solo las 'abierta').
    List<Subasta> findByEstado(String estado);

    // Lista subastas abiertas filtrando por categoría.
    // Lo usamos para mostrar solo las subastas que el usuario puede ver
    // según su categoría.
    List<Subasta> findByEstadoAndCategoria(String estado, String categoria);
}
