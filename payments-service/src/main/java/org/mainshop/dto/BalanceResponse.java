package org.mainshop.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record BalanceResponse(
        @JsonProperty("user_id") UUID userId,
        Long balance,
        String currency
) {
}
