package com.store.order.test;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    private final IdempotencyTestPublisher publisher;

    public TestController(
            IdempotencyTestPublisher publisher) {

        this.publisher = publisher;
    }

    @PostMapping("/test/idempotency")
    public String testIdempotency() {

        publisher.publishDuplicateEvent();

        return "Duplicate event published";
    }
}