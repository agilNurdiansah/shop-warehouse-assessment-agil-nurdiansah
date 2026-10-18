package com.geli.warehouse.service.impl;

import com.geli.warehouse.common.exception.BadRequestException;
import com.geli.warehouse.common.exception.ResourceNotFoundException;
import com.geli.warehouse.dto.request.inventory.UpdateStockRequest;
import com.geli.warehouse.dto.response.inventory.InventoryResponse;
import com.geli.warehouse.model.entity.Inventory;
import com.geli.warehouse.model.entity.ItemVariant;
import com.geli.warehouse.repository.InventoryRepository;
import com.geli.warehouse.repository.ItemVariantRepository;
import com.geli.warehouse.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ItemVariantRepository itemVariantRepository;

    @Override
    @Transactional(readOnly = true)
    public InventoryResponse getInventoryByVariantId(Long variantId) {
        Inventory inventory = inventoryRepository.findByVariantId(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for variant id: " + variantId));
        return mapToInventoryResponse(inventory);
    }

    @Override
    @Transactional
    public InventoryResponse updateStock(Long variantId, UpdateStockRequest request) {
        Inventory inventory = inventoryRepository.findByVariantId(variantId)
                .orElseGet(() -> {
                    ItemVariant variant = itemVariantRepository.findById(variantId)
                            .orElseThrow(() -> new ResourceNotFoundException("Variant not found with id: " + variantId));
                    Inventory newInv = Inventory.builder()
                            .variant(variant)
                            .quantity(0)
                            .reservedQuantity(0)
                            .reorderThreshold(5)
                            .build();
                    variant.setInventory(newInv);
                    return inventoryRepository.save(newInv);
                });

        int currentQty = inventory.getQuantity();
        int newQty;

        switch (request.getOperation()) {
            case ADD -> {
                newQty = currentQty + request.getQuantity();
                inventory.setLastRestockedAt(Instant.now());
            }
            case DEDUCT -> {
                if (currentQty < request.getQuantity()) {
                    throw new BadRequestException("Cannot deduct " + request.getQuantity() +
                            " units. Current stock is only " + currentQty);
                }
                newQty = currentQty - request.getQuantity();
            }
            case SET -> {
                newQty = request.getQuantity();
                if (newQty > currentQty) {
                    inventory.setLastRestockedAt(Instant.now());
                }
            }
            default -> throw new BadRequestException("Invalid stock operation");
        }

        inventory.setQuantity(newQty);

        if (request.getReorderThreshold() != null && request.getReorderThreshold() >= 0) {
            inventory.setReorderThreshold(request.getReorderThreshold());
        }

        Inventory updated = inventoryRepository.save(inventory);
        return mapToInventoryResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryResponse> getAllInventories() {
        return inventoryRepository.findAll().stream()
                .map(this::mapToInventoryResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryResponse> getLowStockAlerts() {
        return inventoryRepository.findLowStockInventories().stream()
                .map(this::mapToInventoryResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryResponse> getOutOfStockItems() {
        return inventoryRepository.findOutOfStockInventories().stream()
                .map(this::mapToInventoryResponse)
                .toList();
    }

    private InventoryResponse mapToInventoryResponse(Inventory inv) {
        ItemVariant variant = inv.getVariant();
        Long variantId = variant != null ? variant.getId() : null;
        String variantSku = variant != null ? variant.getSku() : null;
        String variantName = variant != null ? variant.getVariantName() : null;

        Long itemId = (variant != null && variant.getItem() != null) ? variant.getItem().getId() : null;
        String itemSku = (variant != null && variant.getItem() != null) ? variant.getItem().getSku() : null;
        String itemName = (variant != null && variant.getItem() != null) ? variant.getItem().getName() : null;

        return InventoryResponse.builder()
                .id(inv.getId())
                .variantId(variantId)
                .variantSku(variantSku)
                .variantName(variantName)
                .itemId(itemId)
                .itemSku(itemSku)
                .itemName(itemName)
                .quantity(inv.getQuantity())
                .reservedQuantity(inv.getReservedQuantity())
                .availableQuantity(inv.getAvailableQuantity())
                .reorderThreshold(inv.getReorderThreshold())
                .outOfStock(inv.isOutOfStock())
                .lowStock(inv.isLowStock())
                .lastRestockedAt(inv.getLastRestockedAt())
                .updatedAt(inv.getUpdatedAt())
                .build();
    }
}
