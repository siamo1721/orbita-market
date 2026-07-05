package org.mainshop.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.mainshop.enums.ProductType;

import java.util.Map;

public record CreateOrderRequest(
        @JsonProperty("product_type") ProductType productType,
        Long price,
        Map<String, Object> payload
) {
}