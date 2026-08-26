package com.store.inventory.event;

import java.util.UUID;

public record InventoryReservedEvent(

        UUID eventId,
        String eventType,
        Long orderId,

        Long productId,

        int quantity

) {
}