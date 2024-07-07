package com.eshop.payment.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Entity
@Getter
@Setter
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private Long orderId;
    private String paymentLink;
    private float amount;
    @Enumerated(EnumType.ORDINAL)
    private PaymentStatus paymentStatus;
    private Date paymentLinkCreationTime;
    private String gatewayPaymentId;
}
