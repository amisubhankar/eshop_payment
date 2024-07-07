package com.eshop.payment.dto;

import com.eshop.payment.model.PaymentStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PaymentStatusResponseDto {
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;
    private Long orderId;
}
