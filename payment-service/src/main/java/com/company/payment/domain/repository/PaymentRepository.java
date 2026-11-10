package com.company.payment.domain.repository;

import com.company.payment.domain.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {

    Optional<Payment> findByIdempotencyKey(String idempotencyKey);

    List<Payment> findByUserIdOrderByCreatedAtDesc(String userId);

    List<Payment> findByDealId(String dealId);
}
