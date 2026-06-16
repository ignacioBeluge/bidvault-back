package com.bidvault.api.repository;

import com.bidvault.api.entity.RemateItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RemateItemRepository extends JpaRepository<RemateItem, Integer> {

    // El remate de un ítem específico
    Optional<RemateItem> findByItem(Integer item);

    // El ítem que está en remate activo de una subasta — lo manejamos por item
    Optional<RemateItem> findByItemAndEnRemate(Integer item, String enRemate);
}