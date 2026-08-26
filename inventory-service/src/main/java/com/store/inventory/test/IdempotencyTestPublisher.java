package com.store.inventory.test;

import com.store.inventory.event.PaymentFailedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class IdempotencyTestPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public IdempotencyTestPublisher(
            KafkaTemplate<String, Object> kafkaTemplate) {

        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishDuplicateEvent() {

        PaymentFailedEvent event =
                new PaymentFailedEvent(
                        UUID.fromString(
                                "a848cf78-b193-48fc-9349-b01ae1ea28c9"
                        ),
                        "PaymentFailed",
                        1003L,
                        1L,
                        8,
                        800.0,
                        "Payment declined for order 1003"
                );

        kafkaTemplate.send(
                "payment.failed",
                "1003",
                event
        );
    }
}