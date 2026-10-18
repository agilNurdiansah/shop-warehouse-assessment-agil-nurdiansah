package com.geli.warehouse.service;

import com.geli.warehouse.dto.request.item.CreateItemRequest;
import com.geli.warehouse.dto.request.item.UpdateItemRequest;
import com.geli.warehouse.dto.response.item.ItemResponse;
import com.geli.warehouse.model.enums.ItemStatus;

import java.util.List;

public interface ItemService {
    ItemResponse createItem(CreateItemRequest request);
    ItemResponse getItemById(Long id);
    ItemResponse getItemBySku(String sku);
    List<ItemResponse> getAllItems(String keyword, ItemStatus status);
    ItemResponse updateItem(Long id, UpdateItemRequest request);
    void deleteItem(Long id);
}
