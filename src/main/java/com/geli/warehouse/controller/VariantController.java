package com.geli.warehouse.controller;

import com.geli.warehouse.common.response.ApiResponse;
import com.geli.warehouse.dto.request.item.CreateVariantRequest;
import com.geli.warehouse.dto.request.item.UpdateVariantRequest;
import com.geli.warehouse.dto.response.item.VariantResponse;
import com.geli.warehouse.service.VariantService;
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
@RequiredArgsConstructor
@Tag(name = "Variants", description = "Item variant management (sizes, colors, pricing)")
public class VariantController {

    private final VariantService variantService;

    @PostMapping("/api/v1/items/{itemId}/variants")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Add a new variant to an item with initial stock")
    public ResponseEntity<ApiResponse<VariantResponse>> addVariant(
            @PathVariable Long itemId,
            @Valid @RequestBody CreateVariantRequest request) {
        VariantResponse response = variantService.addVariant(itemId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Variant added successfully", response));
    }

    @GetMapping("/api/v1/items/{itemId}/variants")
    @Operation(summary = "Get all variants for an item")
    public ResponseEntity<ApiResponse<List<VariantResponse>>> getVariantsByItemId(@PathVariable Long itemId) {
        List<VariantResponse> response = variantService.getVariantsByItemId(itemId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/api/v1/variants/{id}")
    @Operation(summary = "Get variant details by ID")
    public ResponseEntity<ApiResponse<VariantResponse>> getVariantById(@PathVariable Long id) {
        VariantResponse response = variantService.getVariantById(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/api/v1/variants/{id}")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Update variant details (price, color, size)")
    public ResponseEntity<ApiResponse<VariantResponse>> updateVariant(
            @PathVariable Long id,
            @Valid @RequestBody UpdateVariantRequest request) {
        VariantResponse response = variantService.updateVariant(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Variant updated successfully", response));
    }

    @DeleteMapping("/api/v1/variants/{id}")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Delete variant")
    public ResponseEntity<ApiResponse<Void>> deleteVariant(@PathVariable Long id) {
        variantService.deleteVariant(id);
        return ResponseEntity.ok(ApiResponse.ok("Variant deleted successfully", null));
    }
}
