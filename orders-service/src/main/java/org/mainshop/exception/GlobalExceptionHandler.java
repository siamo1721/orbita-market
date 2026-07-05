package org.mainshop.exception;

import org.mainshop.dto.ErrorResponse;
import org.mainshop.enums.ErrorType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
        ErrorResponse response = new ErrorResponse(
                ex.getErrorType(),
                ex.getMessage(),
                LocalDateTime.now()
        );

        HttpStatus status = mapToHttpStatus(ex.getErrorType());
        return new ResponseEntity<>(response, status);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse response = new ErrorResponse(
                ErrorType.INTERNAL_ERROR,
                "Внутренняя ошибка сервера",
                LocalDateTime.now()
        );
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private HttpStatus mapToHttpStatus(ErrorType errorType) {
        return switch (errorType) {
            case ORDER_NOT_FOUND -> HttpStatus.NOT_FOUND;          // 404
            case INVALID_PAYLOAD, MISSING_USER_ID, INVALID_PRICE, UNKNOWN_PRODUCT_TYPE -> HttpStatus.BAD_REQUEST; // 400
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }

}
