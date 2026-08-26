package com.store.order.kafka;

import com.store.order.event.InventoryFailedEvent;
import com.store.order.event.PaymentCompletedEvent;
import com.store.order.event.PaymentFailedEvent;
import com.store.order.model.Order;
import com.store.order.model.OrderStatus;
import com.store.order.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class OrderEventListener {

    private final OrderRepository orderRepository;

    public OrderEventListener(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @KafkaListener(topics = "inventory.failed", groupId = "order-service")
    public void handleInventoryFailed(InventoryFailedEvent event) {

        log.warn( "Received InventoryFailedEvent: orderId={}, productId={}, quantity={}, reason={}",
                event.orderId(),
                event.productId(),
                event.requestedQuantity(),
                event.reason() );

        Order order = orderRepository
                        .findById(event.orderId())
                        .orElseThrow(() -> new RuntimeException("Order not found: " + event.orderId()));

        order.setStatus(OrderStatus.INVENTORY_FAILED);
        orderRepository.save(order);

        log.info("Order marked as FAILED: orderId={}", event.orderId());

    }

    @KafkaListener(topics = "payment.completed", groupId = "order-service")
    public void handlePaymentCompleted(PaymentCompletedEvent event) {

        log.info("Received PaymentCompletedEvent: eventId={}, orderId={}, productId={}, quantity={}, amount={}",
                event.eventId(),
                event.orderId(),
                event.productId(),
                event.quantity(),
                event.amount());

        Order order = orderRepository
                        .findById(event.orderId())
                        .orElseThrow(() -> new RuntimeException("Order not found: " + event.orderId()));

        order.setStatus(OrderStatus.PAYMENT_COMPLETED);
        orderRepository.save(order);

        log.info("Order marked as COMPLETED: orderId={}", event.orderId());
    }

    @KafkaListener(topics = "payment.failed", groupId = "order-service")
    public void handlePaymentFailed(PaymentFailedEvent event) {

        log.warn( "Received PaymentFailedEvent: orderId={}, productId={}, quantity={}, amount={}, reason={}",
                event.orderId(),
                event.productId(),
                event.quantity(),
                event.amount(),
                event.reason() );

        Order order =
                orderRepository
                        .findById(event.orderId())
                        .orElseThrow(() -> new RuntimeException("Order not found: " + event.orderId()));
        order.setStatus(OrderStatus.PAYMENT_FAILED);
        orderRepository.save(order);
        log.info("Order marked as FAILED: orderId={}", event.orderId());
    }
}
