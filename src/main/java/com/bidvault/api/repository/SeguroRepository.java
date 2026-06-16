package com.bidvault.api.repository;

import com.bidvault.api.entity.Seguro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SeguroRepository extends JpaRepository<Seguro, Integer> {
    Optional<Seguro> findByProducto(Integer productoId);
}
