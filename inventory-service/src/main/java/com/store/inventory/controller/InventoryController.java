package com.store.inventory.controller;

import com.store.inventory.model.Inventory;
import com.store.inventory.service.InventoryService;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(
            InventoryService inventoryService) {

        this.inventoryService =
                inventoryService;
    }

    @GetMapping("/{productId}")
    public Inventory getInventory(
            @PathVariable Long productId) {

        return inventoryService.getInventory(
                productId
        );
    }
}