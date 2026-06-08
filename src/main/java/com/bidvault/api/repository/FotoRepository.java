package com.bidvault.api.repository;

import com.bidvault.api.entity.Foto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FotoRepository extends JpaRepository<Foto, Integer> {

    // Trae todas las fotos de un producto
    List<Foto> findByProducto(Integer producto);
}