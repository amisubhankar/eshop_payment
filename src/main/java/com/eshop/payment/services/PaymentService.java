package com.eshop.payment.services;

import com.eshop.payment.dto.*;
import com.eshop.payment.exceptions.UnableToGeneratePaymentLinkException;
import com.eshop.payment.model.Payment;
import com.eshop.payment.model.PaymentStatus;
import com.eshop.payment.repository.iPaymentRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Date;
import java.util.Optional;

@Service
public class PaymentService {
    @Autowired
    iPaymentGateway paymentGateway;
    @Autowired
    RestTemplate restTemplate;

    @Autowired
    iPaymentRepository paymentRepository;

    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    ObjectMapper objectMapper;

    public PaymentResponseDto createPaymentLink(Long orderId) throws UnableToGeneratePaymentLinkException {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        //1. Get Order details
        OrderResponseDto orderDetails = restTemplate.getForObject("http://order/order/" + orderId, OrderResponseDto.class);
        if(orderDetails.getOrderStatus().equals(OrderStatus.ORDER_COMPLETED)){
            return new PaymentResponseDto("Payment is already completed against this order", null);
        }
        else if (orderDetails.getOrderStatus().equals(OrderStatus.ORDER_CANCELLED)){
            return new PaymentResponseDto("Order is cancelled", null);
        }

        //2. Call createPaymentLink()
        String paymentLink = paymentGateway.createPaymentLink(orderId, orderDetails.getAmount());

        //3. Checking if any Payment entry is there related to this order_id
        Optional<Payment> optionalPayment = paymentRepository.findByOrderId(orderId);
        Payment payment;
        payment = optionalPayment.orElseGet(Payment::new);

        //4. Create or Update Payment instance
        payment.setAmount(orderDetails.getAmount());
        payment.setPaymentLink(paymentLink);
        payment.setPaymentLinkCreationTime(new Date());
        payment.setPaymentStatus(PaymentStatus.PAYMENT_INITIATED);
        payment.setOrderId(orderId);

        //5. Create Payment entry in DB
        Payment savedPayment = paymentRepository.save(payment);

        //6. Update Payment id in Order table
        restTemplate.exchange("http://order/order/paymentId?orderId={orderId}&paymentId={paymentId}",
                HttpMethod.PUT,
                requestEntity,
                String.class,
                orderId,
                savedPayment.getId());

        return new PaymentResponseDto(paymentLink, "Payment Link Generated");
    }

    public PaymentStatus checkPaymentStatus(String paymentId) {
        //Fetch payment status
        PaymentStatusResponseDto paymentStatus = paymentGateway.checkPaymentStatus(paymentId);

        // Fetch payment entry by order id
        Payment payment = paymentRepository.findByOrderId(paymentStatus.getOrderId()).get();
        payment.setGatewayPaymentId(paymentId);

        //Directly fetch userEmail from order id
        String toEmail = paymentRepository.findUserEmail(paymentStatus.getOrderId()).
                orElseThrow(() -> new RuntimeException("User email not found !!"));

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        //For sending email through kafka
        String message;

        if(paymentStatus.getPaymentStatus().equals(PaymentStatus.PAYMENT_SUCCESS)){
            System.out.println("Success");
            //Update Payment status
            payment.setPaymentStatus(PaymentStatus.PAYMENT_SUCCESS);
            //Update Order table
            restTemplate.exchange("http://order/order/status?orderId={orderId}&status={status}",
                    HttpMethod.PUT,
                    requestEntity,
                    OrderResponseDto.class,
                    paymentStatus.getOrderId(),
                    OrderStatus.ORDER_COMPLETED);

            message  = getEmailFormatForKafka(toEmail, PaymentStatus.PAYMENT_SUCCESS, paymentStatus.getOrderId());
        }else{
            System.out.println("Failed");
            //Update Payment status
            payment.setPaymentStatus(PaymentStatus.PAYMENT_FAILED);
            //Update Order table
            restTemplate.exchange("http://order/order/status?orderId={orderId}&status={status}",
                    HttpMethod.PUT,
                    requestEntity,
                    OrderResponseDto.class,
                    paymentStatus.getOrderId(),
                    OrderStatus.ORDER_DECLINED);

            message = getEmailFormatForKafka(toEmail, PaymentStatus.PAYMENT_FAILED, paymentStatus.getOrderId());
        }

        paymentRepository.save(payment);

        kafkaTemplate.send("sendEshopEmail", message);

        return paymentStatus.getPaymentStatus();
    }

    private String getEmailFormatForKafka(String toEmail, PaymentStatus paymentStatus, Long orderId) {
        EmailFormatDto message = new EmailFormatDto();
        message.setToEmail(toEmail);

        if(paymentStatus.equals(PaymentStatus.PAYMENT_SUCCESS)){
            message.setSubject("Payment Success || Eshop");
            message.setContent("Hi\n\nYour payment against order - " + orderId + " is success.\n\nHappy Shopping !!");
        }else{
            message.setSubject("Payment Failed || Eshop");
            message.setContent("Hi\n\nYour payment against order - " + orderId + " is failed. Please try again.\n\nHappy Shopping !!");
        }

        try {
            return objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
