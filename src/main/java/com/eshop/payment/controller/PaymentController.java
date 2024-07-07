package com.eshop.payment.controller;

import com.eshop.payment.dto.PaymentResponseDto;
import com.eshop.payment.exceptions.UnableToGeneratePaymentLinkException;
import com.eshop.payment.model.PaymentStatus;
import com.eshop.payment.services.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payment")
public class PaymentController {
    @Autowired
    PaymentService paymentService;

    @PostMapping("/createLink/{orderId}")
    public ResponseEntity<PaymentResponseDto> createPaymentLink(@PathVariable Long orderId) throws UnableToGeneratePaymentLinkException {
        PaymentResponseDto paymentReponse = paymentService.createPaymentLink(orderId);

        return ResponseEntity.ok().body(paymentReponse);
    }

    @GetMapping("/status")
    public PaymentStatus checkPaymentStatus(@RequestParam(name = "razorpay_payment_id") String paymentId){
        return paymentService.checkPaymentStatus(paymentId);
    }
}
