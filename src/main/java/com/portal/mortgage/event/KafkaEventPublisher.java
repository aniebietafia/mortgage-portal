package com.portal.mortgage.event;

import com.portal.mortgage.event.model.EventMetadata;
import com.portal.mortgage.event.model.LoanApplicationEvent;
import com.portal.mortgage.mapper.ApplicationMapper;
import com.portal.mortgage.entity.Application;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaEventPublisher {

    private final KafkaTemplate<String, LoanApplicationEvent> kafkaTemplate;
    private final ApplicationMapper applicationMapper; // To convert Entity to DTO

    /**
     * Publishes an event to a Kafka topic.
     *
     * @param topic The destination topic.
     * @param eventType A string identifying the type of event (e.g., "APPLICATION_CREATED").
     * @param application The core application entity payload.
     * @param traceId The correlation ID for tracing.
     */
    public void publishEvent(String topic, String eventType, Application application, String traceId) {
        try {
            // 1. Build the structured event payload
            LoanApplicationEvent event = LoanApplicationEvent.builder()
                    .metadata(EventMetadata.builder()
                            .traceId(traceId)
                            .eventType(eventType)
                            .timestamp(Instant.now().toEpochMilli())
                            .build())
                    .payload(applicationMapper.toDto(application)) // Use DTO for the payload
                    .build();

            // 2. Create a Kafka message with the traceId in the header
            Message<LoanApplicationEvent> message = MessageBuilder
                    .withPayload(event)
                    .setHeader(KafkaHeaders.TOPIC, topic)
                    .setHeader(KafkaHeaders.KEY, application.getId())
                    .setHeader("X-Trace-Id", traceId) // Custom header for observability
                    .build();

            // 3. Send asynchronously and handle the result with a callback
            kafkaTemplate.send(message).whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Successfully sent event to topic '{}' with key '{}' and traceId '{}'. Offset: {}",
                            topic, application.getId(), traceId, result.getRecordMetadata().offset());
                } else {
                    log.error("Failed to send event to topic '{}' with key '{}' and traceId '{}'. Error: {}",
                            topic, application.getId(), traceId, ex.getMessage());
                    // Here you could implement fallback logic (e.g., publish to a dead-letter queue or another system)
                }
            });
        } catch (Exception e) {
            log.error("An unexpected error occurred while preparing the Kafka event for topic '{}' and key '{}'.",
                    topic, application.getId(), e);
        }
    }
}