package org.mainshop.kafka.event;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record OrderPaymentCompleted(
        @JsonProperty("event_id") UUID eventId,
        @JsonProperty("order_id") UUID orderId,
        @JsonProperty("user_id") UUID userId,
        Long amount,
        @JsonProperty("new_balance") Long newBalance
) {
}
