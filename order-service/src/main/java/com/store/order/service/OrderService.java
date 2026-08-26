package com.store.order.service;

import com.store.order.dto.CreateOrderRequest;
import com.store.order.event.OrderCreatedEvent;
import com.store.order.model.IdempotencyKey;
import com.store.order.model.Order;
import com.store.order.repository.IdempotencyKeyRepository;
import com.store.order.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Slf4j
public class OrderService {

    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;
    private final OrderRepository orderRepository;
    private final IdempotencyKeyRepository idempotencyKeyRepository;

    public OrderService(KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate, OrderRepository orderRepository,
                        IdempotencyKeyRepository idempotencyKeyRepository) {

        this.kafkaTemplate = kafkaTemplate;
        this.orderRepository = orderRepository;
        this.idempotencyKeyRepository = idempotencyKeyRepository;
    }

    public Long createOrder(CreateOrderRequest createOrderRequest, String idempotencyKey) {
        log.info("Creating order: idempotencyKey={}, productId={}, quantity={}", idempotencyKey, createOrderRequest.productId(), createOrderRequest.quantity());

        // 1. Check whether request was already processed
        Optional<IdempotencyKey> existingKey = idempotencyKeyRepository.findByIdempotencyKey(idempotencyKey);
        if (existingKey.isPresent()) {
            Long existingOrderId = existingKey.get().getOrderId();
            log.warn("Duplicate order request detected: idempotencyKey={}, existingOrderId={}", idempotencyKey, existingOrderId);
            return existingOrderId;
        }

        // 2. Create Order
        Order order =
                new Order(
                        createOrderRequest.productId(),
                        createOrderRequest.quantity(),
                        createOrderRequest.customerEmail()
                );
        orderRepository.save(order);
        log.info("Order saved to database: orderId={}, status={}", order.getId(), order.getStatus());

        // 3. Save idempotency record
        IdempotencyKey idempotencyRecord = new IdempotencyKey(idempotencyKey, order.getId());
        idempotencyKeyRepository.save(idempotencyRecord);
        log.info("Idempotency key saved: key={}, orderId={}", idempotencyKey, order.getId());


        // 4. Create Kafka event
        OrderCreatedEvent event =
                new OrderCreatedEvent(
                        UUID.randomUUID(),
                        "OrderCreated",
                        order.getId(),
                        createOrderRequest.productId(),
                        createOrderRequest.quantity(),
                        createOrderRequest.customerEmail()
                );

        log.info("Publishing OrderCreatedEvent: eventId={}, orderId={}, topic=order.created", event.eventId(), event.orderId());

        // 5. Publish event to Kafka
        kafkaTemplate.send(
                "order.created",
                String.valueOf(order.getId()),
                event
        );

        log.info("OrderCreatedEvent published: orderId={}", order.getId());

        return order.getId();
    }
}
