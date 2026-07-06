package org.mainshop.kafka.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.mainshop.enums.FailureReasonType;

import java.util.UUID;

public record OrderPaymentFailed(
        @JsonProperty("event_id") UUID eventId,
        @JsonProperty("order_id") UUID orderId,
        @JsonProperty("user_id") UUID userId,
        FailureReasonType reason
) {
}
