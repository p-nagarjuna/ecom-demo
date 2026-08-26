package com.store.payment.test;

import com.store.payment.event.InventoryReservedEvent;
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

        InventoryReservedEvent event =
                new InventoryReservedEvent(
                        UUID.fromString(
                                "164c0abb-e2ff-418c-bc3e-20778a3f7c88"
                        ),
                        "InventoryReserved",
                        1001L,
                        1L,
                        1
                );

        kafkaTemplate.send(
                "inventory.reserved",
                "1001",
                event
        );
    }
}