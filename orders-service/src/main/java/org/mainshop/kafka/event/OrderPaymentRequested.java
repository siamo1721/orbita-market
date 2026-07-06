package org.mainshop.kafka.event;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

public record OrderPaymentRequested(
        @JsonProperty("event_id") UUID eventId,
        @JsonProperty("order_id") UUID orderId,
        @JsonProperty("user_id") UUID userId,
        Long amount,
        @JsonProperty("occurred_at") Instant occurredAt
) {
}
