package com.bidvault.api.repository;

import com.bidvault.api.entity.Persona;
import org.springframework.data.jpa.repository.JpaRepository;

// JpaRepository<Entity, TipoDeLaPK>
public interface PersonaRepository extends JpaRepository<Persona, Integer> {
    
}