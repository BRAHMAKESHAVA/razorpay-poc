package org.backend.repository;

import org.backend.model.PaymentRefund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRefundRepository extends JpaRepository<PaymentRefund, Long> {

    List<PaymentRefund> findByPaymentId(Long paymentId);

}