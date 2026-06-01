package com.bidvault.api.repository;

import com.bidvault.api.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, Integer> {
    // El id de cliente es el mismo que el de persona (por @MapsId),
    // así que findById(idPersona) trae el cliente.
}
