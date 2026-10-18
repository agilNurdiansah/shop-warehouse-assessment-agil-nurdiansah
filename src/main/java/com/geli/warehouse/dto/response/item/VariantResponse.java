package com.geli.warehouse.dto.response.item;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VariantResponse {

    private Long id;
    private Long itemId;
    private String sku;
    private String variantName;
    private String size;
    private String color;
    private BigDecimal price;
    private Integer availableStock;
    private Integer totalStock;
    private boolean outOfStock;
    private boolean lowStock;
    private Instant createdAt;
    private Instant updatedAt;
}
