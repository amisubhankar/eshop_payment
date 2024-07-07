package com.eshop.payment.services;

import com.eshop.payment.dto.PaymentStatusResponseDto;
import com.eshop.payment.exceptions.UnableToGeneratePaymentLinkException;
import com.eshop.payment.model.PaymentStatus;
import org.springframework.stereotype.Component;

@Component
public interface iPaymentGateway {
    String createPaymentLink(Long orderId, float amount) throws UnableToGeneratePaymentLinkException;

    PaymentStatusResponseDto checkPaymentStatus(String paymentId);
}
