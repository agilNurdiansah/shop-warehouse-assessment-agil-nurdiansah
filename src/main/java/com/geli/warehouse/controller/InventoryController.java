package com.geli.warehouse.controller;

import com.geli.warehouse.common.response.ApiResponse;
import com.geli.warehouse.dto.request.inventory.UpdateStockRequest;
import com.geli.warehouse.dto.response.inventory.InventoryResponse;
import com.geli.warehouse.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory", description = "Stock level management and alerts")
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Initialize or set inventory stock for a variant")
    public ResponseEntity<ApiResponse<InventoryResponse>> createInventory(
            @Valid @RequestBody com.geli.warehouse.dto.request.inventory.CreateInventoryRequest request) {
        InventoryResponse response = inventoryService.createInventory(request);
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED)
                .body(ApiResponse.ok("Inventory initialized successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get all inventory records")
    public ResponseEntity<ApiResponse<List<InventoryResponse>>> getAllInventories() {
        List<InventoryResponse> response = inventoryService.getAllInventories();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/variant/{variantId}")
    @Operation(summary = "Get inventory level for a specific variant")
    public ResponseEntity<ApiResponse<InventoryResponse>> getInventoryByVariantId(@PathVariable Long variantId) {
        InventoryResponse response = inventoryService.getInventoryByVariantId(variantId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/variant/{variantId}")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Update inventory stock (ADD, DEDUCT, or SET)")
    public ResponseEntity<ApiResponse<InventoryResponse>> updateStock(
            @PathVariable Long variantId,
            @Valid @RequestBody UpdateStockRequest request) {
        InventoryResponse response = inventoryService.updateStock(variantId, request);
        return ResponseEntity.ok(ApiResponse.ok("Stock updated successfully", response));
    }

    @GetMapping("/low-stock")
    @Operation(summary = "Get items with stock at or below reorder threshold")
    public ResponseEntity<ApiResponse<List<InventoryResponse>>> getLowStockAlerts() {
        List<InventoryResponse> response = inventoryService.getLowStockAlerts();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/out-of-stock")
    @Operation(summary = "Get items that are currently out of stock")
    public ResponseEntity<ApiResponse<List<InventoryResponse>>> getOutOfStockItems() {
        List<InventoryResponse> response = inventoryService.getOutOfStockItems();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
