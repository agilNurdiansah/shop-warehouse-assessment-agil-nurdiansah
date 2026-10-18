package com.geli.warehouse.dto.response.inventory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryResponse {

    private Long id;
    private Long variantId;
    private String variantSku;
    private String variantName;
    private Long itemId;
    private String itemSku;
    private String itemName;
    private Integer quantity;
    private Integer reservedQuantity;
    private Integer availableQuantity;
    private Integer reorderThreshold;
    private boolean outOfStock;
    private boolean lowStock;
    private Instant lastRestockedAt;
    private Instant updatedAt;
}
