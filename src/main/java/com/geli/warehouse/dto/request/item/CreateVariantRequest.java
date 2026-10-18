package com.geli.warehouse.dto.request.item;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateVariantRequest {

    @NotBlank(message = "Variant SKU is required")
    private String sku;

    @NotBlank(message = "Variant name is required")
    private String variantName;

    private String size;

    private String color;

    @NotNull(message = "Variant price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;

    @Builder.Default
    private Integer initialStock = 0;

    @Builder.Default
    private Integer reorderThreshold = 5;
}
