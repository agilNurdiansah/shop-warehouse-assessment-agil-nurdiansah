package com.geli.warehouse.service;

import com.geli.warehouse.dto.request.inventory.UpdateStockRequest;
import com.geli.warehouse.dto.response.inventory.InventoryResponse;

import java.util.List;

public interface InventoryService {
    InventoryResponse getInventoryByVariantId(Long variantId);
    InventoryResponse updateStock(Long variantId, UpdateStockRequest request);
    List<InventoryResponse> getAllInventories();
    List<InventoryResponse> getLowStockAlerts();
    List<InventoryResponse> getOutOfStockItems();
}
