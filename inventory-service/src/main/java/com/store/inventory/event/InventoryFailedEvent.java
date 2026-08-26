package com.store.inventory.event;

import java.util.UUID;

public record InventoryFailedEvent(

        UUID eventId,
        String eventType,
        Long orderId,

        Long productId,

        int requestedQuantity,

        String reason

) {
}