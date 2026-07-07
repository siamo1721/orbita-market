package org.mainshop.exception;

import org.mainshop.dto.ErrorResponse;
import org.mainshop.enums.ErrorType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.UUID;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        if (ex.getRequiredType() != null && UUID.class.equals(ex.getRequiredType())) {
            ErrorResponse response = new ErrorResponse(
                    ErrorType.INVALID_PAYLOAD,
                    "Некорректный формат UUID",
                    LocalDateTime.now()
            );
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        ErrorResponse response = new ErrorResponse(
                ErrorType.INVALID_PAYLOAD,
                "Некорректный параметр запроса",
                LocalDateTime.now()
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

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
