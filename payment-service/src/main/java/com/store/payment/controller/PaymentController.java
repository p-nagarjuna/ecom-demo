package com.store.payment.controller;

import com.store.payment.model.Payment;
import com.store.payment.service.PaymentService;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(
            PaymentService paymentService) {

        this.paymentService =
                paymentService;
    }

    @GetMapping("/{orderId}")
    public Payment getPayment(
            @PathVariable Long orderId) {

        return paymentService.getPayment(
                orderId
        );
    }
}