package com.bidvault.api.repository;

import com.bidvault.api.entity.Catalogo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CatalogoRepository extends JpaRepository<Catalogo, Integer> {
    Optional<Catalogo> findBySubasta(Integer subasta);
}
