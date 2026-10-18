package com.geli.warehouse.repository;

import com.geli.warehouse.model.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByVariantId(Long variantId);

    Optional<Inventory> findByVariant_Sku(String sku);

    @Query("SELECT inv FROM Inventory inv WHERE (inv.quantity - inv.reservedQuantity) <= inv.reorderThreshold")
    List<Inventory> findLowStockInventories();

    @Query("SELECT inv FROM Inventory inv WHERE (inv.quantity - inv.reservedQuantity) <= 0")
    List<Inventory> findOutOfStockInventories();
}
