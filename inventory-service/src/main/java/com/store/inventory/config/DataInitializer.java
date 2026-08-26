package com.store.inventory.config;

import com.store.inventory.model.Inventory;
import com.store.inventory.repository.InventoryRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initializeInventory(
            InventoryRepository repository) {

        return args -> {

            if (repository.count() == 0) {

                repository.save(
                        new Inventory(1L, 10)
                );

                repository.save(
                        new Inventory(2L, 20)
                );

                repository.save(
                        new Inventory(3L, 50)
                );
            }
        };
    }
}