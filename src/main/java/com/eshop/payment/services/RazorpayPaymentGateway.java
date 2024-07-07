package com.eshop.payment.services;

import com.eshop.payment.dto.PaymentStatusResponseDto;
import com.eshop.payment.exceptions.UnableToGeneratePaymentLinkException;
import com.eshop.payment.model.PaymentStatus;
import com.razorpay.Payment;
import com.razorpay.PaymentLink;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class RazorpayPaymentGateway implements iPaymentGateway{
    @Autowired
    RazorpayClient razorpayClient;
    @Override
    public String createPaymentLink(Long orderId, float amount) throws UnableToGeneratePaymentLinkException {
        JSONObject paymentLinkRequest = new JSONObject();
        paymentLinkRequest.put("amount",amount);
        paymentLinkRequest.put("currency","INR");
        paymentLinkRequest.put("accept_partial",false);
        paymentLinkRequest.put("expire_by", Instant.now().plus(20, ChronoUnit.MINUTES).getEpochSecond());
//        paymentLinkRequest.put("reference_id", orderId);
        paymentLinkRequest.put("description","Payment for order no #" + orderId);


        JSONObject notes = new JSONObject();
        notes.put("my_order","Eshop Order For #" + orderId);

        paymentLinkRequest.put("notes",notes);
        paymentLinkRequest.put("callback_url","http://localhost:8085/payment/status");
        paymentLinkRequest.put("callback_method","get");

        PaymentLink payment = null ;
        try {
            payment = razorpayClient.paymentLink.create(paymentLinkRequest);
        } catch (RazorpayException e) {
            throw new UnableToGeneratePaymentLinkException("Unable to create link from Razorpay Client");
        }

        return payment.get("short_url");
    }

    @Override
    public PaymentStatusResponseDto checkPaymentStatus(String paymentId) {
        Payment payment = null;
        try {
            payment = razorpayClient.payments.fetch(paymentId);
        } catch (RazorpayException e) {
            throw new RuntimeException("Unable to fetch details");
        }

        //Fetching Order id
        String stringOrderId = payment.get("notes").toString().replace("\"", "").replace("}", "").split("#")[1];
        Long orderId = Long.parseLong(stringOrderId);

        if (payment.get("status").equals("captured")){
            return new PaymentStatusResponseDto(PaymentStatus.PAYMENT_SUCCESS, orderId);
        }else{
            return new PaymentStatusResponseDto(PaymentStatus.PAYMENT_FAILED, orderId);
        }
    }
}
