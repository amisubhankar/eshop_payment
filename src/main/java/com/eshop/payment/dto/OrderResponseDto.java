package com.eshop.payment.dto;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderResponseDto {
    private Long orderId;
    private float amount;
    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;
}
