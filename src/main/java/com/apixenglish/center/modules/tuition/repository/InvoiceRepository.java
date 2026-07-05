package com.apixenglish.center.modules.tuition.repository;

import com.apixenglish.center.modules.tuition.entity.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    Optional<Invoice> findByInvoiceNoAndDeletedAtIsNull(String invoiceNo);

    List<Invoice> findByStudentIdAndDeletedAtIsNull(UUID studentId);

    @Query("SELECT i FROM Invoice i " +
           "WHERE i.deletedAt IS NULL " +
           "AND (cast(:search as string) IS NULL OR " +
           "     LOWER(i.title) LIKE LOWER(CONCAT('%', cast(:search as string), '%')) OR " +
           "     LOWER(i.invoiceNo) LIKE LOWER(CONCAT('%', cast(:search as string), '%')))")
    Page<Invoice> searchInvoices(@Param("search") String search, Pageable pageable);

    @Query("SELECT MAX(i.invoiceNo) FROM Invoice i WHERE i.invoiceNo LIKE 'INV%'")
    String findMaxInvoiceNo();
}
