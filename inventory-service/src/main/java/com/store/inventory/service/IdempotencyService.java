package com.store.inventory.service;

import com.store.inventory.entity.ProcessedEvent;
import com.store.inventory.repository.ProcessedEventRepository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
public class IdempotencyService {

    private final ProcessedEventRepository processedEventRepository;

    public IdempotencyService(
            ProcessedEventRepository processedEventRepository) {

        this.processedEventRepository =
                processedEventRepository;
    }

    public boolean isAlreadyProcessed(UUID eventId) {

        boolean exists =
                processedEventRepository.existsById(eventId);

        log.info(
                "Idempotency check: eventId={}, alreadyProcessed={}",
                eventId,
                exists
        );

        return exists;
    }

    public void markAsProcessed(
            UUID eventId,
            String eventType) {

        processedEventRepository.save(
                new ProcessedEvent(
                        eventId,
                        eventType
                )
        );

        log.info(
                "Event marked as processed: eventId={}, eventType={}",
                eventId,
                eventType
        );
    }
}