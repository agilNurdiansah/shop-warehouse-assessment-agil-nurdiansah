package com.geli.warehouse.service.impl;

import com.geli.warehouse.common.exception.BadRequestException;
import com.geli.warehouse.common.exception.ResourceNotFoundException;
import com.geli.warehouse.dto.request.item.CreateItemRequest;
import com.geli.warehouse.dto.request.item.CreateVariantRequest;
import com.geli.warehouse.dto.request.item.UpdateItemRequest;
import com.geli.warehouse.dto.response.item.ItemResponse;
import com.geli.warehouse.dto.response.item.VariantResponse;
import com.geli.warehouse.model.entity.Inventory;
import com.geli.warehouse.model.entity.Item;
import com.geli.warehouse.model.entity.ItemVariant;
import com.geli.warehouse.model.enums.ItemStatus;
import com.geli.warehouse.repository.ItemRepository;
import com.geli.warehouse.repository.ItemVariantRepository;
import com.geli.warehouse.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final ItemVariantRepository itemVariantRepository;

    @Override
    @Transactional
    public ItemResponse createItem(CreateItemRequest request) {
        if (itemRepository.existsBySku(request.getSku())) {
            throw new BadRequestException("Item with SKU '" + request.getSku() + "' already exists");
        }

        Item item = Item.builder()
                .sku(request.getSku().toUpperCase().trim())
                .name(request.getName().trim())
                .description(request.getDescription())
                .basePrice(request.getBasePrice())
                .status(ItemStatus.ACTIVE)
                .variants(new ArrayList<>())
                .build();

        if (request.getVariants() != null && !request.getVariants().isEmpty()) {
            for (CreateVariantRequest vReq : request.getVariants()) {
                if (itemVariantRepository.existsBySku(vReq.getSku())) {
                    throw new BadRequestException("Variant with SKU '" + vReq.getSku() + "' already exists");
                }

                ItemVariant variant = ItemVariant.builder()
                        .sku(vReq.getSku().toUpperCase().trim())
                        .variantName(vReq.getVariantName().trim())
                        .size(vReq.getSize())
                        .color(vReq.getColor())
                        .price(vReq.getPrice())
                        .build();

                int stockQty = vReq.getInitialStock() != null ? vReq.getInitialStock() : 0;
                int threshold = vReq.getReorderThreshold() != null ? vReq.getReorderThreshold() : 5;

                Inventory inventory = Inventory.builder()
                        .quantity(stockQty)
                        .reservedQuantity(0)
                        .reorderThreshold(threshold)
                        .lastRestockedAt(stockQty > 0 ? Instant.now() : null)
                        .build();

                variant.setInventory(inventory);
                item.addVariant(variant);
            }
        }

        Item savedItem = itemRepository.save(item);
        return mapToItemResponse(savedItem);
    }

    @Override
    @Transactional(readOnly = true)
    public ItemResponse getItemById(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + id));
        return mapToItemResponse(item);
    }

    @Override
    @Transactional(readOnly = true)
    public ItemResponse getItemBySku(String sku) {
        Item item = itemRepository.findBySku(sku.toUpperCase().trim())
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with SKU: " + sku));
        return mapToItemResponse(item);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemResponse> getAllItems(String keyword, ItemStatus status) {
        List<Item> items;
        if (StringUtils.hasText(keyword)) {
            items = itemRepository.searchByKeyword(keyword.trim());
        } else if (status != null) {
            items = itemRepository.findByStatus(status);
        } else {
            items = itemRepository.findAll();
        }
        return items.stream().map(this::mapToItemResponse).toList();
    }

    @Override
    @Transactional
    public ItemResponse updateItem(Long id, UpdateItemRequest request) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + id));

        if (StringUtils.hasText(request.getName())) {
            item.setName(request.getName().trim());
        }
        if (request.getDescription() != null) {
            item.setDescription(request.getDescription());
        }
        if (request.getBasePrice() != null) {
            item.setBasePrice(request.getBasePrice());
        }
        if (request.getStatus() != null) {
            item.setStatus(request.getStatus());
        }

        Item updated = itemRepository.save(item);
        return mapToItemResponse(updated);
    }

    @Override
    @Transactional
    public void deleteItem(Long id) {
        if (!itemRepository.existsById(id)) {
            throw new ResourceNotFoundException("Item not found with id: " + id);
        }
        itemRepository.deleteById(id);
    }

    public ItemResponse mapToItemResponse(Item item) {
        List<VariantResponse> variantResponses = item.getVariants().stream()
                .map(this::mapToVariantResponse)
                .toList();

        int totalStock = variantResponses.stream()
                .mapToInt(VariantResponse::getAvailableStock)
                .sum();

        return ItemResponse.builder()
                .id(item.getId())
                .sku(item.getSku())
                .name(item.getName())
                .description(item.getDescription())
                .basePrice(item.getBasePrice())
                .status(item.getStatus())
                .totalStock(totalStock)
                .variants(variantResponses)
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }

    public VariantResponse mapToVariantResponse(ItemVariant variant) {
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
