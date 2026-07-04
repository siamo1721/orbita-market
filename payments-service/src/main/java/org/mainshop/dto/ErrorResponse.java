package org.mainshop.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.mainshop.enums.ErrorType;

import java.time.LocalDateTime;

public record ErrorResponse(
        @JsonProperty("error_code") ErrorType errorType,
        String message,
        LocalDateTime timestamp
) {
}
