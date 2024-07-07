package com.eshop.payment.controllerAdvice;

import com.eshop.payment.dto.PaymentResponseDto;
import com.eshop.payment.exceptions.UnableToGeneratePaymentLinkException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AnyControllerAdvice {
    @ExceptionHandler(UnableToGeneratePaymentLinkException.class)
    public ResponseEntity<PaymentResponseDto> handlePaymentLinkGenerationFailed(UnableToGeneratePaymentLinkException ex){
        return ResponseEntity.badRequest().body(new PaymentResponseDto(ex.getMessage(), null));
    }
}
