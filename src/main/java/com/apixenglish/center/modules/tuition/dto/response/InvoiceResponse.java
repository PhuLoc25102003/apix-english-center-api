package com.apixenglish.center.modules.tuition.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceResponse {
    private UUID id;
    private UUID studentId;
    private String studentName;
    private String studentCode;
    private UUID classId;
    private String className;
    private String classCode;
    private UUID enrollmentId;
    private UUID tuitionPackageId;
    private String tuitionPackageName;
    private String invoiceNo;
    private String title;
    private String description;
    private LocalDate billingStartMonth;
    private LocalDate billingEndMonth;
    private Integer numberOfMonths;
    private BigDecimal monthlyFee;
    private BigDecimal subtotalAmount;
    private String discountType;
    private BigDecimal discountValue;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal remainingAmount;
    private LocalDate dueDate;
    private String status;
}
