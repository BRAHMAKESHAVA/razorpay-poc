package org.backend.repository;

import org.backend.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByProviderOrderId(String providerOrderId);

    Optional<Payment> findByProviderPaymentId(String providerPaymentId);

    Optional<Payment> findByBookingId(Long bookingId);

    boolean existsByProviderPaymentId(String razorpayOrderId);

    List<Payment> findByBookingIdIn(List<Long> bookingIds);


}