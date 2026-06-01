package com.bidvault.api.repository;

import com.bidvault.api.entity.SesionSubasta;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SesionSubastaRepository extends JpaRepository<SesionSubasta, Integer> {

    // Busca si el cliente tiene una sesión activa en CUALQUIER subasta.
    // Si ya tiene una activa, no puede conectarse a otra (regla de negocio).
    Optional<SesionSubasta> findByClienteAndActiva(Integer cliente, String activa);
}