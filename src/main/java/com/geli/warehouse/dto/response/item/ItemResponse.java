package com.geli.warehouse.dto.response.item;

import com.geli.warehouse.model.enums.ItemStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemResponse {

    private Long id;
    private String sku;
    private String name;
    private String description;
    private BigDecimal basePrice;
    private ItemStatus status;
    private Integer totalStock;
    private List<VariantResponse> variants;
    private Instant createdAt;
    private Instant updatedAt;
}
