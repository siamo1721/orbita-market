package org.mainshop.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mainshop.entity.Order;
import org.mainshop.enums.OrderStatus;
import org.mainshop.inbox.Inbox;
import org.mainshop.inbox.InboxRepository;
import org.mainshop.kafka.event.OrderPaymentCompleted;
import org.mainshop.kafka.event.OrderPaymentFailed;
import org.mainshop.repository.OrderRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentResultConsumer {

    private final ObjectMapper objectMapper;
    private final InboxRepository inboxRepository;
    private final OrderRepository orderRepository;

    @KafkaListener(topics = KafkaTopics.ORDER_PAYMENT_COMPLETED, groupId = "orders-service")
    @Transactional
    public void onPaymentCompleted(String message) {
        try {
            OrderPaymentCompleted event = objectMapper.readValue(message, OrderPaymentCompleted.class);

            if (inboxRepository.existsByEventId(event.eventId())) {
                log.debug("Пропускаем дубликат completed-события {}", event.eventId());
                return;
            }

            Order order = orderRepository.findByOrderId(event.orderId())
                    .orElseThrow(() -> new IllegalStateException(
                            "Order not found for completed payment: " + event.orderId()));

            if (order.getStatus() == OrderStatus.PAYMENT_PENDING) {
                order.setStatus(OrderStatus.PAID);
                orderRepository.save(order);
            }

            saveInbox(event.eventId(), KafkaTopics.ORDER_PAYMENT_COMPLETED);
            log.info("Заказ {} переведён в статус PAID", event.orderId());
        } catch (Exception e) {
            log.error("Ошибка обработки completed-события: {}", message, e);
            throw new RuntimeException(e);
        }
    }

    @KafkaListener(topics = KafkaTopics.ORDER_PAYMENT_FAILED, groupId = "orders-service")
    @Transactional
    public void onPaymentFailed(String message) {
        try {
            OrderPaymentFailed event = objectMapper.readValue(message, OrderPaymentFailed.class);

            if (inboxRepository.existsByEventId(event.eventId())) {
                log.debug("Пропускаем дубликат failed-события {}", event.eventId());
                return;
            }

            Order order = orderRepository.findByOrderId(event.orderId())
                    .orElseThrow(() -> new IllegalStateException(
                            "Order not found for failed payment: " + event.orderId()));

            if (order.getStatus() == OrderStatus.PAYMENT_PENDING) {
                order.setStatus(OrderStatus.PAYMENT_FAILED);
                order.setFailureReason(event.reason());
                orderRepository.save(order);
            }

            saveInbox(event.eventId(), KafkaTopics.ORDER_PAYMENT_FAILED);
            log.info("Заказ {} переведён в статус PAYMENT_FAILED, причина={}", event.orderId(), event.reason());
        } catch (Exception e) {
            log.error("Ошибка обработки failed-события: {}", message, e);
            throw new RuntimeException(e);
        }
    }

    private void saveInbox(UUID eventId, String eventType) {
        Inbox inbox = new Inbox();
        inbox.setEventId(eventId);
        inbox.setEventType(eventType);
        inbox.setProcessedAt(Instant.now());
        inboxRepository.save(inbox);
    }
}
