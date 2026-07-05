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
                .invoiceNo(invoice.getInvoiceNo())
                .title(invoice.getTitle())
                .description(invoice.getDescription())
                .totalAmount(invoice.getTotalAmount())
                .paidAmount(invoice.getPaidAmount())
                .remainingAmount(invoice.getRemainingAmount())
                .dueDate(invoice.getDueDate())
                .status(invoice.getStatus())
                .build();
    }
}
