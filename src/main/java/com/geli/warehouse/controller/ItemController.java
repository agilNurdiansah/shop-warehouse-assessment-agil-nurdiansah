package com.geli.warehouse.controller;

import com.geli.warehouse.common.response.ApiResponse;
import com.geli.warehouse.dto.request.item.CreateItemRequest;
import com.geli.warehouse.dto.request.item.UpdateItemRequest;
import com.geli.warehouse.dto.response.item.ItemResponse;
import com.geli.warehouse.model.enums.ItemStatus;
import com.geli.warehouse.service.ItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/items")
@RequiredArgsConstructor
@Tag(name = "Items", description = "Shop warehouse items catalog management")
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Create a new item with optional initial variants and stock")
    public ResponseEntity<ApiResponse<ItemResponse>> createItem(@Valid @RequestBody CreateItemRequest request) {
        ItemResponse response = itemService.createItem(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Item created successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get all items (supports optional search by keyword and filter by status)")
    public ResponseEntity<ApiResponse<List<ItemResponse>>> getAllItems(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) ItemStatus status) {
        List<ItemResponse> response = itemService.getAllItems(keyword, status);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get item by ID")
    public ResponseEntity<ApiResponse<ItemResponse>> getItemById(@PathVariable Long id) {
        ItemResponse response = itemService.getItemById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/sku/{sku}")
    @Operation(summary = "Get item by SKU")
    public ResponseEntity<ApiResponse<ItemResponse>> getItemBySku(@PathVariable String sku) {
        ItemResponse response = itemService.getItemBySku(sku);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{id}")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Update item details")
    public ResponseEntity<ApiResponse<ItemResponse>> updateItem(
            @PathVariable Long id,
            @Valid @RequestBody UpdateItemRequest request) {
        ItemResponse response = itemService.updateItem(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Item updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Delete item and associated variants/inventory")
    public ResponseEntity<ApiResponse<Void>> deleteItem(@PathVariable Long id) {
        itemService.deleteItem(id);
        return ResponseEntity.ok(ApiResponse.ok("Item deleted successfully", null));
    }
}
