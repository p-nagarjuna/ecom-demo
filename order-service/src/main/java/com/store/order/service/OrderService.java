package com.store.order.service;

import com.store.order.dto.CreateOrderRequest;
import com.store.order.event.OrderCreatedEvent;
import com.store.order.model.Order;
import com.store.order.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Slf4j
public class OrderService {

    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;
    private final OrderRepository orderRepository;

    public OrderService(KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate, OrderRepository orderRepository) {

        this.kafkaTemplate = kafkaTemplate;
        this.orderRepository = orderRepository;
    }

    public Long createOrder(CreateOrderRequest createOrderRequest) {
        log.info("Creating order: productId={}, quantity={}", createOrderRequest.productId(), createOrderRequest.quantity());

        /*
         * Create and save Order.
         *
         * Database generates the order ID.
         */
        Order order =
                new Order(
                        createOrderRequest.productId(),
                        createOrderRequest.quantity(),
                        createOrderRequest.customerEmail()
                );

        orderRepository.save(order);

//        log.info("Order saved to database: orderId={}, status={}", order.getId(), order.getStatus());

            // Create Kafka event
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

            // Publish event to Kafka
            kafkaTemplate.send(
                    "order.created",
                    String.valueOf(order.getId()),
                    event
            );

        log.info("OrderCreatedEvent published: orderId={}",order.getId());

            return order.getId();
    }
}
