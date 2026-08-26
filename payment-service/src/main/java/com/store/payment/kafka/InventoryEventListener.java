package com.store.payment.kafka;

import com.store.payment.event.InventoryReservedEvent;
import com.store.payment.event.PaymentCompletedEvent;
import com.store.payment.event.PaymentFailedEvent;
import com.store.payment.exception.PaymentFailedException;
import com.store.payment.service.IdempotencyService;
import com.store.payment.service.PaymentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class InventoryEventListener {

    private final PaymentService paymentService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final IdempotencyService idempotencyService;

    @KafkaListener(topics = "inventory.reserved", groupId = "payment-service")
    public void handleInventoryReserved(InventoryReservedEvent event) {

        log.info("Received InventoryReservedEvent: orderId={}, productId={}, quantity={}", event.orderId(), event.productId(), event.quantity());

        if (idempotencyService.isAlreadyProcessed(event.eventId())) {

            log.warn("Duplicate InventoryReservedEvent detected. Skipping payment: eventId={}, orderId={}", event.eventId(), event.orderId());

            return;
        }

        try {
            log.info("Processing payment: orderId={}, productId={}, quantity={}", event.orderId(), event.productId(), event.quantity());

            PaymentCompletedEvent completedEvent = paymentService.processPayment(event);

            log.info("Payment processed successfully: orderId={}, amount={}", event.orderId(), completedEvent.amount());
            log.info("Publishing PaymentCompletedEvent: orderId={}, topic=payment.completed", event.orderId());

            kafkaTemplate.send("payment.completed", String.valueOf(event.orderId()), completedEvent);

            log.info("PaymentCompletedEvent published successfully: orderId={}", event.orderId());

            idempotencyService.markAsProcessed(event.eventId(), event.eventType());

        } catch (PaymentFailedException ex) {
            double amount = calculateAmount(event);
            log.warn("Payment failed: orderId={}, productId={}, quantity={}, amount={}, reason={}", event.orderId(), event.productId(), event.quantity(), amount, ex.getMessage());

            PaymentFailedEvent failedEvent = new PaymentFailedEvent(UUID.randomUUID(), "PaymentFailed", event.orderId(), event.productId(), event.quantity(), calculateAmount(event), ex.getMessage());

            log.info("Publishing PaymentFailedEvent: orderId={}, topic=payment.failed", event.orderId());

            kafkaTemplate.send("payment.failed", String.valueOf(event.orderId()), failedEvent);

            log.info("PaymentFailedEvent published successfully: orderId={}", event.orderId());

            idempotencyService.markAsProcessed(event.eventId(), event.eventType());
        }
    }

    private double calculateAmount(InventoryReservedEvent event) {

        double unitPrice = switch (event.productId().intValue()) {

            case 1 -> 100.0;
            case 2 -> 200.0;
            case 3 -> 50.0;

            default -> 0.0;
        };

        double amount = unitPrice * event.quantity();

        log.debug("Calculated payment amount: orderId={}, productId={}, unitPrice={}, quantity={}, amount={}", event.orderId(), event.productId(), unitPrice, event.quantity(), amount);

        return amount;
    }
}