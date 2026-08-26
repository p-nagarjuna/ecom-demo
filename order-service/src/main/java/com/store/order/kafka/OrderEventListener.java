package com.store.order.kafka;

import com.store.order.event.InventoryFailedEvent;
import com.store.order.event.PaymentCompletedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class OrderEventListener {

    @KafkaListener(topics = "inventory.failed", groupId = "order-service")
    public void handleInventoryFailed(InventoryFailedEvent event) {


        log.warn(
                "Received InventoryFailedEvent: orderId={}, productId={}, quantity={}, reason={}",
                event.orderId(),
                event.productId(),
                event.requestedQuantity(),
                event.reason()
        );

        /*
         * Update order status to FAILED.
         *
         * Order Service currently does not have PostgreSQL,
         * so for now this is where the status update logic
         * should be connected to the current OrderService.
         */

        log.info(
                "Order marked as FAILED: orderId={}",
                event.orderId()
        );

    }

    @KafkaListener(topics = "payment.completed", groupId = "order-service")
    public void handlePaymentFailed(PaymentCompletedEvent event) {

        log.info(
                "Received PaymentCompletedEvent: eventId={}, orderId={}, productId={}, quantity={}, amount={}",
                event.eventId(),
                event.orderId(),
                event.productId(),
                event.quantity(),
                event.amount()
        );

        /*
         * Update order status to COMPLETED.
         *
         * Connect this to the existing OrderService
         * once we look at the current order implementation.
         */

        log.info(
                "Order marked as COMPLETED: orderId={}",
                event.orderId()
        );

    }
}
