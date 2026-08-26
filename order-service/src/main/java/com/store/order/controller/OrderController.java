package com.store.order.controller;

import com.store.order.dto.CreateOrderRequest;
import com.store.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(
            OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody CreateOrderRequest request) {

        log.info(
                "Received create order request: productId={}, quantity={}, customerEmail={}",
                request.productId(),
                request.quantity(),
                request.customerEmail()
        );

        long orderId = orderService.createOrder(request, idempotencyKey);

        log.info("Order processed successfully: orderId={}, idempotencyKey={}", orderId, idempotencyKey);

        // Return response immediately
        return ResponseEntity
                .accepted()
                .body(
                        Map.of(
                                "orderId", orderId,
                                "status", "ORDER_CREATED",
                                "message",
                                "Order accepted for processing"
                        )
                );
    }
}