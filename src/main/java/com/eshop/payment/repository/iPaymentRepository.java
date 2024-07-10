package com.eshop.payment.repository;

import com.eshop.payment.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface iPaymentRepository extends JpaRepository<Payment, UUID> {
    Optional<Payment> findByOrderId(Long orderId);

    @Query(value = "select email from user_auth u, eshop_order eo where eo.id= :orderId and eo.user_id=u.id",
            nativeQuery = true)
    Optional<String> findUserEmail(Long orderId);
}
