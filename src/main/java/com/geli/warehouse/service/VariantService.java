package com.geli.warehouse.service;

import com.geli.warehouse.dto.request.item.CreateVariantRequest;
import com.geli.warehouse.dto.request.item.UpdateVariantRequest;
import com.geli.warehouse.dto.response.item.VariantResponse;

import java.util.List;

public interface VariantService {
    VariantResponse addVariant(Long itemId, CreateVariantRequest request);
    VariantResponse getVariantById(Long variantId);
    List<VariantResponse> getVariantsByItemId(Long itemId);
    VariantResponse updateVariant(Long variantId, UpdateVariantRequest request);
    void deleteVariant(Long variantId);
}
