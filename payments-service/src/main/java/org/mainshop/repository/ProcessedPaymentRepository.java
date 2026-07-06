package org.mainshop.repository;

import org.mainshop.entity.ProcessedPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProcessedPaymentRepository extends JpaRepository<ProcessedPayment, Long> {

    boolean existsByOrderId(UUID orderId);
}
