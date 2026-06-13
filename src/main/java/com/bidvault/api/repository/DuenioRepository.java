package com.bidvault.api.repository;

import com.bidvault.api.entity.Duenio;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DuenioRepository extends JpaRepository<Duenio, Integer> {

    // existsById ya viene incluido en JpaRepository.
    // Lo usamos para saber si el cliente ya está registrado como dueño.
}
