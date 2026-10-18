package com.geli.warehouse.repository;

import com.geli.warehouse.model.entity.ItemVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemVariantRepository extends JpaRepository<ItemVariant, Long> {

    Optional<ItemVariant> findBySku(String sku);

    boolean existsBySku(String sku);

    List<ItemVariant> findByItemId(Long itemId);
}
