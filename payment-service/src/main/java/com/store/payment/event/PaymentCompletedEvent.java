package com.store.payment.event;

import java.util.UUID;

public record PaymentCompletedEvent(
        UUID eventId,
        String eventType,
        Long orderId,
        Long productId,
        int quantity,
        double amount
) {
}