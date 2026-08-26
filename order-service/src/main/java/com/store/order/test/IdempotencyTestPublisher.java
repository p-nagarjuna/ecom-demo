package com.store.order.test;

import com.store.order.event.OrderCreatedEvent;
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

        OrderCreatedEvent event =
                new OrderCreatedEvent(
                        UUID.fromString(
                                "d70e2ddb-5687-4797-a60f-249c0291b76f"
                        ),
                        "OrderCreated",
                        1001L,
                        1L,
                        2,
                        "user1@example.com"
                );

        kafkaTemplate.send(
                "order.created",
                "1001",
                event
        );
    }
}