package com.bidvault.api.repository;

import com.bidvault.api.entity.Multa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MultaRepository extends JpaRepository<Multa, Integer> {
    List<Multa> findByCliente(Integer clienteId);
}
