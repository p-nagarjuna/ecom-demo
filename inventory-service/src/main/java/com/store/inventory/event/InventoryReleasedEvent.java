package com.store.inventory.event;

import java.util.UUID;

public record InventoryReleasedEvent(

        UUID eventId,
        String eventType,
        Long orderId,

        Long productId,

        int quantity

) {
}