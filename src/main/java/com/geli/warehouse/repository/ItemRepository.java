package com.geli.warehouse.repository;

import com.geli.warehouse.model.entity.Item;
import com.geli.warehouse.model.enums.ItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    Optional<Item> findBySku(String sku);

    boolean existsBySku(String sku);

    List<Item> findByStatus(ItemStatus status);

    @Query("SELECT i FROM Item i WHERE LOWER(i.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(i.sku) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Item> searchByKeyword(@Param("keyword") String keyword);
}
