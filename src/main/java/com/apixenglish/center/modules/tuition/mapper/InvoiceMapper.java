package com.apixenglish.center.modules.tuition.mapper;

import com.apixenglish.center.modules.tuition.dto.response.InvoiceResponse;
import com.apixenglish.center.modules.tuition.entity.Invoice;
import org.springframework.stereotype.Component;

@Component
public class InvoiceMapper {

    public InvoiceResponse toResponse(Invoice invoice) {
        if (invoice == null) {
            return null;
        }

        return InvoiceResponse.builder()
                .id(invoice.getId())
                .studentId(invoice.getStudent() != null ? invoice.getStudent().getId() : null)
                .studentName(invoice.getStudent() != null ? invoice.getStudent().getFullName() : null)
                .studentCode(invoice.getStudent() != null ? invoice.getStudent().getStudentCode() : null)
                .classId(invoice.getClazz() != null ? invoice.getClazz().getId() : null)
                .className(invoice.getClazz() != null ? invoice.getClazz().getName() : null)
                .classCode(invoice.getClazz() != null ? invoice.getClazz().getClassCode() : null)
                .enrollmentId(invoice.getEnrollment() != null ? invoice.getEnrollment().getId() : null)
                .tuitionPackageId(invoice.getTuitionPackage() != null ? invoice.getTuitionPackage().getId() : null)
                .tuitionPackageName(invoice.getTuitionPackage() != null ? invoice.getTuitionPackage().getName() : null)
                .invoiceNo(invoice.getInvoiceNo())
                .title(invoice.getTitle())
                .description(invoice.getDescription())
                .billingStartMonth(invoice.getBillingStartMonth())
                .billingEndMonth(invoice.getBillingEndMonth())
                .numberOfMonths(invoice.getNumberOfMonths())
                .monthlyFee(invoice.getMonthlyFee())
                .subtotalAmount(invoice.getSubtotalAmount())
                .discountType(invoice.getDiscountType())
                .discountValue(invoice.getDiscountValue())
                .discountAmount(invoice.getDiscountAmount())
                .totalAmount(invoice.getTotalAmount())
                .paidAmount(invoice.getPaidAmount())
                .remainingAmount(invoice.getRemainingAmount())
                .dueDate(invoice.getDueDate())
                .status(invoice.getStatus())
                .build();
    }
}
