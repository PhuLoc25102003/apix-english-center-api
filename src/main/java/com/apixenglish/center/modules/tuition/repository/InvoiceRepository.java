package com.apixenglish.center.modules.tuition.repository;

import com.apixenglish.center.modules.tuition.entity.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    Optional<Invoice> findByInvoiceNoAndDeletedAtIsNull(String invoiceNo);

    List<Invoice> findByStudentIdAndDeletedAtIsNull(UUID studentId);

    List<Invoice> findByClazzIdAndDeletedAtIsNull(UUID classId);
    List<Invoice> findByEnrollmentIdAndDeletedAtIsNull(UUID enrollmentId);

    @Query("SELECT COUNT(i) > 0 FROM Invoice i " +
           "WHERE i.deletedAt IS NULL AND i.student.id = :studentId AND i.clazz.id = :classId " +
           "AND i.status NOT IN ('CANCELLED', 'REFUNDED') " +
           "AND i.billingStartMonth <= :billingEndMonth AND i.billingEndMonth >= :billingStartMonth")
    boolean existsOverlappingBillingPeriod(
            @Param("studentId") UUID studentId,
            @Param("classId") UUID classId,
            @Param("billingStartMonth") LocalDate billingStartMonth,
            @Param("billingEndMonth") LocalDate billingEndMonth
    );

    @Query("SELECT i FROM Invoice i LEFT JOIN i.clazz c LEFT JOIN c.campus campus " +
           "WHERE i.deletedAt IS NULL " +
           "AND (cast(:search as string) IS NULL OR " +
           "     LOWER(i.title) LIKE LOWER(CONCAT('%', cast(:search as string), '%')) OR " +
           "     LOWER(i.invoiceNo) LIKE LOWER(CONCAT('%', cast(:search as string), '%')) OR " +
           "     LOWER(i.student.fullName) LIKE LOWER(CONCAT('%', cast(:search as string), '%')) OR " +
           "     LOWER(i.student.studentCode) LIKE LOWER(CONCAT('%', cast(:search as string), '%'))) " +
           "AND (:studentId IS NULL OR i.student.id = :studentId) " +
           "AND (:classId IS NULL OR c.id = :classId) " +
           "AND (:campusId IS NULL OR campus.id = :campusId) " +
           "AND (:status IS NULL OR " +
           "     (:status = 'OVERDUE' AND i.dueDate < CURRENT_DATE " +
           "      AND i.remainingAmount > 0 AND i.status NOT IN ('CANCELLED', 'REFUNDED')) OR " +
           "     (:status <> 'OVERDUE' AND i.status = :status " +
           "      AND (i.status NOT IN ('UNPAID', 'PARTIALLY_PAID') OR i.dueDate >= CURRENT_DATE))) " +
           "AND (:billingMonth IS NULL OR " +
           "     (i.billingStartMonth <= :billingMonth AND i.billingEndMonth >= :billingMonth))")
    Page<Invoice> searchInvoices(
            @Param("search") String search,
            @Param("studentId") UUID studentId,
            @Param("classId") UUID classId,
            @Param("campusId") UUID campusId,
            @Param("billingMonth") LocalDate billingMonth,
            @Param("status") String status,
            Pageable pageable
    );

    @Query("SELECT MAX(i.invoiceNo) FROM Invoice i WHERE i.invoiceNo LIKE 'INV%'")
    String findMaxInvoiceNo();
}
