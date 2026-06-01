package com.bidvault.api.repository;

import com.bidvault.api.entity.Pujo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface PujoRepository extends JpaRepository<Pujo, Integer> {

    // Trae todas las pujas de un ítem ordenadas de mayor a menor importe.
    // La primera de la lista es la oferta ganadora actual.
    List<Pujo> findByItemOrderByImporteDesc(Integer item);

    // Trae la mayor puja de un ítem. Usamos una query custom con TOP 1.
    // La necesitamos para validar que la nueva puja supere a la actual.
    @Query("SELECT p FROM Pujo p WHERE p.item = :item ORDER BY p.importe DESC")
    List<Pujo> findMayorPuja(@Param("item") Integer item);
}