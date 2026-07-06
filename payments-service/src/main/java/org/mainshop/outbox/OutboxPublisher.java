package org.mainshop.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mainshop.enums.OutboxEventType;
import org.mainshop.enums.OutboxStatus;
import org.mainshop.kafka.KafkaTopics;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPublisher {

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void publishPendingEvents() {
        List<Outbox> pendingEvents = outboxRepository.findByStatus(OutboxStatus.PENDING);

        for (Outbox outbox : pendingEvents) {
            try {
                String payload = objectMapper.writeValueAsString(outbox.getPayload());
                String topic = resolveTopic(outbox.getEventType());
                String key = outbox.getEventId().toString();

                kafkaTemplate.send(topic, key, payload).get();

                outbox.setStatus(OutboxStatus.PUBLISHED);
                outbox.setPublishedAt(Instant.now());
                outboxRepository.save(outbox);

                log.info("Outbox-событие {} опубликовано в топик {}", outbox.getEventId(), topic);
            } catch (JsonProcessingException e) {
                log.error("Не удалось сериализовать outbox-событие {}", outbox.getEventId(), e);
                outbox.setStatus(OutboxStatus.FAILED);
                outboxRepository.save(outbox);
            } catch (Exception e) {
                log.warn("Не удалось опубликовать outbox-событие {}, повторим позже", outbox.getEventId(), e);
            }
        }
    }

    private String resolveTopic(OutboxEventType eventType) {
        return switch (eventType) {
            case ORDER_PAYMENT_COMPLETED -> KafkaTopics.ORDER_PAYMENT_COMPLETED;
            case ORDER_PAYMENT_FAILED -> KafkaTopics.ORDER_PAYMENT_FAILED;
        };
    }
}
