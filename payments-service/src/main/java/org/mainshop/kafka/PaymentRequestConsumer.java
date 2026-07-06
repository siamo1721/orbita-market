package org.mainshop.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mainshop.kafka.event.OrderPaymentRequested;
import org.mainshop.service.PaymentProcessingService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentRequestConsumer {

    private final ObjectMapper objectMapper;
    private final PaymentProcessingService paymentProcessingService;

    @KafkaListener(topics = KafkaTopics.ORDER_PAYMENT_REQUESTED, groupId = "payments-service")
    public void onPaymentRequested(String message) {
        try {
            OrderPaymentRequested event = objectMapper.readValue(message, OrderPaymentRequested.class);
            log.info("Получен запрос на оплату заказа {}, сумма={}", event.orderId(), event.amount());
            paymentProcessingService.processPaymentRequested(event);
        } catch (Exception e) {
            log.error("Ошибка обработки requested-события: {}", message, e);
            throw new RuntimeException(e);
        }
    }
}
