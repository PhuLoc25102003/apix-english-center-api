package com.apixenglish.center.modules.tuition.service;

import com.apixenglish.center.common.response.PageResponse;
import com.apixenglish.center.modules.tuition.dto.request.CreateInvoiceRequest;
import com.apixenglish.center.modules.tuition.dto.request.CreatePaymentRequest;
import com.apixenglish.center.modules.tuition.dto.request.UpsertTuitionPackageRequest;
import com.apixenglish.center.modules.tuition.dto.response.InvoicePaymentResponse;
import com.apixenglish.center.modules.tuition.dto.response.InvoiceResponse;
import com.apixenglish.center.modules.tuition.dto.response.TuitionPackageResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;
import java.time.LocalDate;

public interface TuitionService {
    InvoiceResponse createInvoice(CreateInvoiceRequest request);
    PageResponse<InvoiceResponse> getInvoices(
            String search,
            UUID studentId,
            UUID classId,
            UUID campusId,
            LocalDate billingMonth,
            String status,
            Pageable pageable
    );
    List<InvoiceResponse> getInvoicesByStudent(UUID studentId);
    List<InvoiceResponse> getInvoicesByClass(UUID classId);
    InvoicePaymentResponse createPayment(UUID invoiceId, CreatePaymentRequest request);
    List<InvoicePaymentResponse> getPaymentsByInvoice(UUID invoiceId);
    List<TuitionPackageResponse> getActiveTuitionPackages();
    TuitionPackageResponse createTuitionPackage(UpsertTuitionPackageRequest request);
    TuitionPackageResponse updateTuitionPackage(UUID id, UpsertTuitionPackageRequest request);
    void deleteTuitionPackage(UUID id);
}
