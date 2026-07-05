package com.apixenglish.center.modules.tuition.repository;

import com.apixenglish.center.modules.tuition.entity.InvoicePayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvoicePaymentRepository extends JpaRepository<InvoicePayment, UUID> {

    Optional<InvoicePayment> findByPaymentNoAndDeletedAtIsNull(String paymentNo);

    List<InvoicePayment> findByInvoiceIdAndDeletedAtIsNull(UUID invoiceId);

    @Query("SELECT MAX(p.paymentNo) FROM InvoicePayment p WHERE p.paymentNo LIKE 'PAY%'")
    String findMaxPaymentNo();
}
