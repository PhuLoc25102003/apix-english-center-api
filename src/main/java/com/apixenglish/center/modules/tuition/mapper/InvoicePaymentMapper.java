package com.apixenglish.center.modules.tuition.mapper;

import com.apixenglish.center.modules.tuition.dto.response.InvoicePaymentResponse;
import com.apixenglish.center.modules.tuition.entity.InvoicePayment;
import org.springframework.stereotype.Component;

@Component
public class InvoicePaymentMapper {

    public InvoicePaymentResponse toResponse(InvoicePayment payment) {
        if (payment == null) {
            return null;
        }

        return InvoicePaymentResponse.builder()
                .id(payment.getId())
                .invoiceId(payment.getInvoice() != null ? payment.getInvoice().getId() : null)
                .invoiceNo(payment.getInvoice() != null ? payment.getInvoice().getInvoiceNo() : null)
                .paymentNo(payment.getPaymentNo())
                .amount(payment.getAmount())
                .paymentDate(payment.getPaymentDate())
                .paymentMethod(payment.getPaymentMethod())
                .note(payment.getNote())
                .build();
    }
}
