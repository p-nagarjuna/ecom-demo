package com.store.payment.service;

import com.store.payment.event.InventoryReservedEvent;
import com.store.payment.event.PaymentCompletedEvent;
import com.store.payment.exception.PaymentFailedException;
import com.store.payment.model.Payment;
import com.store.payment.repository.PaymentRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentService(
            PaymentRepository paymentRepository) {

        this.paymentRepository =
                paymentRepository;
    }

    @Transactional
    public PaymentCompletedEvent processPayment(
            InventoryReservedEvent event) {

        double unitPrice =
                getProductPrice(event.productId());

        double amount =
                unitPrice * event.quantity();

        if (event.quantity() > 5) {

            throw new PaymentFailedException(
                    "Payment declined for order "
                            + event.orderId()
            );
        }

        Payment payment =
                new Payment(
                        event.orderId(),
                        amount,
                        "SUCCESS"
                );

        paymentRepository.save(payment);

        return new PaymentCompletedEvent(
                UUID.randomUUID(),
                "PaymentCompleted",
                event.orderId(),
                event.productId(),
                event.quantity(),
                amount
        );
    }

    private double getProductPrice(
            Long productId) {

        return switch (productId.intValue()) {

            case 1 -> 100.0;
            case 2 -> 200.0;
            case 3 -> 50.0;

            default ->
                    throw new IllegalArgumentException(
                            "Unknown product: "
                                    + productId
                    );
        };
    }

    public Payment getPayment(Long orderId) {

        return paymentRepository
                .findById(orderId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Payment not found for order "
                                        + orderId
                        )
                );
    }
}