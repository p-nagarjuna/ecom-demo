package com.store.inventory.kafka;

import com.store.inventory.event.InventoryReleasedEvent;
import com.store.inventory.event.PaymentFailedEvent;
import com.store.inventory.service.IdempotencyService;
import com.store.inventory.service.InventoryService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PaymentEventListener {

    private final InventoryService inventoryService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final IdempotencyService idempotencyService;

    public PaymentEventListener(InventoryService inventoryService, KafkaTemplate<String, Object> kafkaTemplate, IdempotencyService idempotencyService) {

        this.inventoryService = inventoryService;
        this.kafkaTemplate = kafkaTemplate;
        this.idempotencyService = idempotencyService;
    }

    @KafkaListener(topics = "payment.failed", groupId = "inventory-service")
    public void handlePaymentFailed(PaymentFailedEvent event) {

        log.warn("Received PaymentFailedEvent: eventId={}, eventType={}, orderId={}, productId={}, quantity={}, reason={}", event.eventId(), event.eventType(), event.orderId(), event.productId(), event.quantity(), event.reason());

        /*
         * Idempotency check
         */
        if (idempotencyService.isAlreadyProcessed(event.eventId())) {

            log.warn("Duplicate PaymentFailedEvent detected. " + "Skipping compensation: eventId={}, orderId={}", event.eventId(), event.orderId());

            return;
        }

        try {

            /*
             * Release inventory
             */
            InventoryReleasedEvent releasedEvent = inventoryService.releaseInventory(event.productId(), event.orderId(), event.quantity());

            log.info("Inventory released after payment failure: " + "eventId={}, orderId={}, productId={}, quantity={}", event.eventId(), event.orderId(), event.productId(), event.quantity());

            /*
             * Publish inventory.released
             */
            kafkaTemplate.send("inventory.released", String.valueOf(event.orderId()), releasedEvent);

            log.info("InventoryReleasedEvent published: " + "orderId={}, topic=inventory.released", event.orderId());

            /*
             * Mark PaymentFailedEvent as processed
             */
            idempotencyService.markAsProcessed(event.eventId(), event.eventType());

        } catch (Exception ex) {

            log.error("Failed to release inventory: " + "eventId={}, orderId={}, reason={}", event.eventId(), event.orderId(), ex.getMessage(), ex);

            /*
             * IMPORTANT:
             * Do NOT mark the event as processed here.
             *
             * If compensation failed, Kafka should be allowed
             * to retry the event.
             */
        }
    }
}