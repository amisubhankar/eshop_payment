package com.eshop.payment.repository;

import com.eshop.payment.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface iPaymentRepository extends JpaRepository<Payment, UUID> {
    Optional<Payment> findByOrderId(Long orderId);
}
