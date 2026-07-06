package org.mainshop.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mainshop.entity.Account;
import org.mainshop.entity.ProcessedPayment;
import org.mainshop.enums.FailureReasonType;
import org.mainshop.enums.OutboxEventType;
import org.mainshop.enums.OutboxStatus;
import org.mainshop.inbox.Inbox;
import org.mainshop.inbox.InboxRepository;
import org.mainshop.kafka.KafkaTopics;
import org.mainshop.kafka.event.OrderPaymentCompleted;
import org.mainshop.kafka.event.OrderPaymentFailed;
import org.mainshop.kafka.event.OrderPaymentRequested;
import org.mainshop.outbox.Outbox;
import org.mainshop.outbox.OutboxRepository;
import org.mainshop.repository.AccountRepository;
import org.mainshop.repository.ProcessedPaymentRepository;
import org.mainshop.service.PaymentProcessingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentProcessingServiceImpl implements PaymentProcessingService {

    private final InboxRepository inboxRepository;
    private final ProcessedPaymentRepository processedPaymentRepository;
    private final AccountRepository accountRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void processPaymentRequested(OrderPaymentRequested event) {
        if (inboxRepository.existsByEventId(event.eventId())) {
            log.debug("Пропускаем дубликат requested-события {}", event.eventId());
            return;
        }

        if (processedPaymentRepository.existsByOrderId(event.orderId())) {
            log.debug("Оплата для заказа {} уже обработана", event.orderId());
            saveInbox(event.eventId());
            return;
        }

        Account account = accountRepository.findByUserId(event.userId()).orElse(null);

        if (account == null || account.getBalance() < event.amount()) {
            log.info("Недостаточно средств для заказа {}, user={}", event.orderId(), event.userId());
            saveProcessedPayment(event);
            saveFailedOutbox(event);
            saveInbox(event.eventId());
            return;
        }

        account.setBalance(account.getBalance() - event.amount());
        accountRepository.save(account);

        saveProcessedPayment(event);
        saveCompletedOutbox(event, account.getBalance());
        saveInbox(event.eventId());

        log.info("Списано {} за заказ {}, новый баланс={}", event.amount(), event.orderId(), account.getBalance());
    }

    private void saveProcessedPayment(OrderPaymentRequested event) {
        ProcessedPayment processedPayment = new ProcessedPayment();
        processedPayment.setOrderId(event.orderId());
        processedPayment.setUserId(event.userId());
        processedPayment.setAmount(event.amount());
        processedPayment.setProcessedAt(Instant.now());
        processedPaymentRepository.save(processedPayment);
    }

    private void saveCompletedOutbox(OrderPaymentRequested event, Long newBalance) {
        UUID resultEventId = UUID.randomUUID();
        OrderPaymentCompleted completed = new OrderPaymentCompleted(
                resultEventId,
                event.orderId(),
                event.userId(),
                event.amount(),
                newBalance
        );
        saveOutbox(resultEventId, OutboxEventType.ORDER_PAYMENT_COMPLETED, completed);
    }

    private void saveFailedOutbox(OrderPaymentRequested event) {
        UUID resultEventId = UUID.randomUUID();
        OrderPaymentFailed failed = new OrderPaymentFailed(
                resultEventId,
                event.orderId(),
                event.userId(),
                FailureReasonType.INSUFFICIENT_BALANCE
        );
        saveOutbox(resultEventId, OutboxEventType.ORDER_PAYMENT_FAILED, failed);
    }

    private void saveOutbox(UUID eventId, OutboxEventType eventType, Object event) {
        Map<String, Object> payload = objectMapper.convertValue(event, new TypeReference<>() {});

        Outbox outbox = new Outbox();
        outbox.setEventId(eventId);
        outbox.setEventType(eventType);
        outbox.setPayload(payload);
        outbox.setStatus(OutboxStatus.PENDING);
        outbox.setCreatedAt(Instant.now());
        outboxRepository.save(outbox);
    }

    private void saveInbox(UUID eventId) {
        Inbox inbox = new Inbox();
        inbox.setEventId(eventId);
        inbox.setEventType(KafkaTopics.ORDER_PAYMENT_REQUESTED);
        inbox.setProcessedAt(Instant.now());
        inboxRepository.save(inbox);
    }
}
