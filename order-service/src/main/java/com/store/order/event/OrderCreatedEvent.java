package com.store.order.event;

import java.util.UUID;

public record OrderCreatedEvent(
        UUID eventId,
        String eventType,
        Long orderId,

        Long productId,

        int quantity,

        String customerEmail

) {
}