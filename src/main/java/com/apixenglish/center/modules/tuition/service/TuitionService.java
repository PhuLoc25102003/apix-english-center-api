package com.apixenglish.center.modules.tuition.service;

import com.apixenglish.center.common.response.PageResponse;
import com.apixenglish.center.modules.tuition.dto.request.CreateInvoiceRequest;
import com.apixenglish.center.modules.tuition.dto.request.CreatePaymentRequest;
import com.apixenglish.center.modules.tuition.dto.response.InvoicePaymentResponse;
import com.apixenglish.center.modules.tuition.dto.response.InvoiceResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface TuitionService {
    InvoiceResponse createInvoice(CreateInvoiceRequest request);
    PageResponse<InvoiceResponse> getInvoices(String search, Pageable pageable);
    List<InvoiceResponse> getInvoicesByStudent(UUID studentId);
    InvoicePaymentResponse createPayment(UUID invoiceId, CreatePaymentRequest request);
    List<InvoicePaymentResponse> getPaymentsByInvoice(UUID invoiceId);
}
