package com.bidvault.api.repository;

import com.bidvault.api.entity.Asistente;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AsistenteRepository extends JpaRepository<Asistente, Integer> {

    // Busca si un cliente ya está registrado como asistente de una subasta.
    // Lo usamos al conectarse: si ya existe, reusamos; si no, lo creamos.
    Optional<Asistente> findByClienteAndSubasta(Integer cliente, Integer subasta);
}
