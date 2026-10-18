package com.geli.warehouse.dto.request.inventory;

import com.geli.warehouse.model.enums.StockOperation;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStockRequest {

    @NotNull(message = "Operation type is required (ADD, DEDUCT, SET)")
    private StockOperation operation;

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity must be greater than or equal to 0")
    private Integer quantity;

    private Integer reorderThreshold;

    private String notes;
}
