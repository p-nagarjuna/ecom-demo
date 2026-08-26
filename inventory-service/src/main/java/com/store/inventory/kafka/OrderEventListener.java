package com.store.inventory.kafka;

import com.store.inventory.event.InventoryFailedEvent;
import com.store.inventory.event.InventoryReservedEvent;
import com.store.inventory.event.OrderCreatedEvent;
import com.store.inventory.exception.InsufficientInventoryException;
import com.store.inventory.service.IdempotencyService;
import com.store.inventory.service.InventoryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class OrderEventListener {

    private final InventoryService inventoryService;
    private final IdempotencyService idempotencyService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = "order.created", groupId = "inventory-service")
    public void handleOrderCreated(OrderCreatedEvent event) {

        log.info("Received OrderCreatedEvent: orderId={}, productId={}, quantity={}", event.orderId(), event.productId(), event.quantity());

        if (idempotencyService.isAlreadyProcessed(event.eventId())) {
            log.warn("Duplicate event detected. Skipping processing: eventId={}, orderId={}", event.eventId(), event.orderId());
            return;
        }

        try {

            InventoryReservedEvent reservedEvent = inventoryService.reserveInventory(event);

            log.info("Inventory reservation successful: orderId={}, productId={}, quantity={}", event.orderId(), event.productId(), event.quantity());
            log.info("Publishing InventoryReservedEvent: orderId={}, topic=inventory.reserved", event.orderId());

            // SUCCESS
            kafkaTemplate.send("inventory.reserved", String.valueOf(event.orderId()), reservedEvent);

            log.info("InventoryReservedEvent published successfully: orderId={}", event.orderId());

            idempotencyService.markAsProcessed(event.eventId(), event.eventType());

        } catch (InsufficientInventoryException ex) {
            log.warn("Inventory reservation failed: orderId={}, productId={}, quantity={}, reason={}", event.orderId(), event.productId(), event.quantity(), ex.getMessage());

            // FAILURE
            InventoryFailedEvent failedEvent = new InventoryFailedEvent(UUID.randomUUID(), "InventoryFailed", event.orderId(), event.productId(), event.quantity(), ex.getMessage());

            log.info("Publishing InventoryFailedEvent: orderId={}, topic=inventory.failed", event.orderId());

            kafkaTemplate.send("inventory.failed", String.valueOf(event.orderId()), failedEvent);

            log.info("InventoryFailedEvent published successfully: orderId={}", event.orderId());

            idempotencyService.markAsProcessed(event.eventId(), event.eventType());

        }
    }
}