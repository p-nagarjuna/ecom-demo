package com.store.payment.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    private Long orderId;

    private double amount;

    private String status;

    public Payment() {
    }

    public Payment(
            Long orderId,
            double amount,
            String status) {

        this.orderId = orderId;
        this.amount = amount;
        this.status = status;
    }

    public Long getOrderId() {
        return orderId;
    }

    public double getAmount() {
        return amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}