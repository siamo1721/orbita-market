package org.mainshop.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.mainshop.enums.OrderStatus;
import org.mainshop.enums.ProductType;

import java.time.Instant;
import java.util.UUID;

public record OrderResponse (
        @JsonProperty("order_id") UUID orderId,
        OrderStatus status,
        @JsonProperty("product_type") ProductType productType,
        Long price,
        @JsonProperty("created_at") Instant createdAt
) {
}
