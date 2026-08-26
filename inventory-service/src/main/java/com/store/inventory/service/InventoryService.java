package com.store.inventory.service;

import com.store.inventory.event.InventoryReleasedEvent;
import com.store.inventory.event.InventoryReservedEvent;
import com.store.inventory.event.OrderCreatedEvent;
import com.store.inventory.exception.InsufficientInventoryException;
import com.store.inventory.model.Inventory;
import com.store.inventory.repository.InventoryRepository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    public InventoryService(
            InventoryRepository inventoryRepository) {

        this.inventoryRepository =
                inventoryRepository;
    }

    public Inventory getInventory(Long productId) {

        return inventoryRepository
                .findById(productId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Inventory not found for product "
                                        + productId
                        )
                );
    }

    @Transactional
    public InventoryReservedEvent reserveInventory(
            OrderCreatedEvent event) {

        log.info("Checking inventory: orderId={}, productId={}, requestedQuantity={}", event.orderId(), event.productId(), event.quantity());

        Inventory inventory =
                getInventory(event.productId());

        if (inventory.getQuantity() < event.quantity()) {
            log.warn("Insufficient inventory: orderId={}, productId={}, requested={}, available={}", event.orderId(), event.productId(), event.quantity(), inventory.getQuantity());

            throw new InsufficientInventoryException(
                    "Insufficient inventory. "
                            + "Available: "
                            + inventory.getQuantity()
                            + ", Requested: "
                            + event.quantity()
            );
        }

        log.info(
                "Inventory available: orderId={}, productId={}, quantity={}",
                event.orderId(),
                event.productId(),
                event.quantity()
        );

        inventory.setQuantity(
                inventory.getQuantity()
                        - event.quantity()
        );

        inventoryRepository.save(inventory);

        return new InventoryReservedEvent(
                UUID.randomUUID(),
                "InventoryReserved",
                event.orderId(),
                event.productId(),
                event.quantity()
        );
    }

    @Transactional
    public InventoryReleasedEvent releaseInventory(
            Long productId,
            Long orderId,
            int quantity) {

        Inventory inventory =
                getInventory(productId);

        inventory.setQuantity(
                inventory.getQuantity()
                        + quantity
        );

        inventoryRepository.save(inventory);

        return new InventoryReleasedEvent(
                UUID.randomUUID(),
                "InventoryReleased",
                orderId,
                productId,
                quantity
        );
    }
}