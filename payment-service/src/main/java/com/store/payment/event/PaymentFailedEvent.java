package com.store.payment.event;

import java.util.UUID;

public record PaymentFailedEvent(

        UUID eventId,
        String eventType,
        Long orderId,

        Long productId,

        int quantity,

        double amount,

        String reason

) {
}