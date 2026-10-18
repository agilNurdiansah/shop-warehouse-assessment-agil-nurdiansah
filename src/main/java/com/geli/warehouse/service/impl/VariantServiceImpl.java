package com.geli.warehouse.service.impl;

import com.geli.warehouse.common.exception.BadRequestException;
import com.geli.warehouse.common.exception.ResourceNotFoundException;
import com.geli.warehouse.dto.request.item.CreateVariantRequest;
import com.geli.warehouse.dto.request.item.UpdateVariantRequest;
import com.geli.warehouse.dto.response.item.VariantResponse;
import com.geli.warehouse.model.entity.Inventory;
import com.geli.warehouse.model.entity.Item;
import com.geli.warehouse.model.entity.ItemVariant;
import com.geli.warehouse.repository.ItemRepository;
import com.geli.warehouse.repository.ItemVariantRepository;
import com.geli.warehouse.service.VariantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VariantServiceImpl implements VariantService {

    private final ItemRepository itemRepository;
    private final ItemVariantRepository itemVariantRepository;

    @Override
    @Transactional
    public VariantResponse addVariant(Long itemId, CreateVariantRequest request) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + itemId));

        if (itemVariantRepository.existsBySku(request.getSku())) {
            throw new BadRequestException("Variant with SKU '" + request.getSku() + "' already exists");
        }

        ItemVariant variant = ItemVariant.builder()
                .sku(request.getSku().toUpperCase().trim())
                .variantName(request.getVariantName().trim())
                .size(request.getSize())
                .color(request.getColor())
                .price(request.getPrice())
                .build();

        int stockQty = request.getInitialStock() != null ? request.getInitialStock() : 0;
        int threshold = request.getReorderThreshold() != null ? request.getReorderThreshold() : 5;

        Inventory inventory = Inventory.builder()
                .quantity(stockQty)
                .reservedQuantity(0)
                .reorderThreshold(threshold)
                .lastRestockedAt(stockQty > 0 ? Instant.now() : null)
                .build();

        variant.setInventory(inventory);
        item.addVariant(variant);

        ItemVariant savedVariant = itemVariantRepository.save(variant);
        return mapToVariantResponse(savedVariant);
    }

    @Override
    @Transactional(readOnly = true)
    public VariantResponse getVariantById(Long variantId) {
        ItemVariant variant = itemVariantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Variant not found with id: " + variantId));
        return mapToVariantResponse(variant);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VariantResponse> getVariantsByItemId(Long itemId) {
        if (!itemRepository.existsById(itemId)) {
            throw new ResourceNotFoundException("Item not found with id: " + itemId);
        }
        return itemVariantRepository.findByItemId(itemId).stream()
                .map(this::mapToVariantResponse)
                .toList();
    }

    @Override
    @Transactional
    public VariantResponse updateVariant(Long variantId, UpdateVariantRequest request) {
        ItemVariant variant = itemVariantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Variant not found with id: " + variantId));

        if (StringUtils.hasText(request.getVariantName())) {
            variant.setVariantName(request.getVariantName().trim());
        }
        if (request.getSize() != null) {
            variant.setSize(request.getSize());
        }
        if (request.getColor() != null) {
            variant.setColor(request.getColor());
        }
        if (request.getPrice() != null) {
            variant.setPrice(request.getPrice());
        }

        ItemVariant updated = itemVariantRepository.save(variant);
        return mapToVariantResponse(updated);
    }

    @Override
    @Transactional
    public void deleteVariant(Long variantId) {
        ItemVariant variant = itemVariantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Variant not found with id: " + variantId));

        if (variant.getItem() != null) {
            variant.getItem().removeVariant(variant);
        }
        itemVariantRepository.delete(variant);
    }

    private VariantResponse mapToVariantResponse(ItemVariant variant) {
        Inventory inv = variant.getInventory();
        int available = inv != null ? inv.getAvailableQuantity() : 0;
        int total = inv != null ? inv.getQuantity() : 0;
        boolean outOfStock = inv == null || inv.isOutOfStock();
        boolean lowStock = inv != null && inv.isLowStock();

        return VariantResponse.builder()
                .id(variant.getId())
                .itemId(variant.getItem() != null ? variant.getItem().getId() : null)
                .sku(variant.getSku())
                .variantName(variant.getVariantName())
                .size(variant.getSize())
                .color(variant.getColor())
                .price(variant.getPrice())
                .availableStock(available)
                .totalStock(total)
                .outOfStock(outOfStock)
                .lowStock(lowStock)
                .createdAt(variant.getCreatedAt())
                .updatedAt(variant.getUpdatedAt())
                .build();
    }
}
