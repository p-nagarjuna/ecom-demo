package com.store.order.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CreateOrderRequest(

        @NotNull
        Long productId,

        @Min(1)
        int quantity,

        @Email
        String customerEmail

) {
}