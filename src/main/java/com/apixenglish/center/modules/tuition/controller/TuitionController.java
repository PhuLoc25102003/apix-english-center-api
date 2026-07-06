package com.apixenglish.center.modules.tuition.controller;

import com.apixenglish.center.common.response.ApiResponse;
import com.apixenglish.center.common.response.PageResponse;
import com.apixenglish.center.modules.tuition.dto.request.CreateInvoiceRequest;
import com.apixenglish.center.modules.tuition.dto.request.CreatePaymentRequest;
import com.apixenglish.center.modules.tuition.dto.request.UpsertTuitionPackageRequest;
import com.apixenglish.center.modules.tuition.dto.response.InvoicePaymentResponse;
import com.apixenglish.center.modules.tuition.dto.response.InvoiceResponse;
import com.apixenglish.center.modules.tuition.dto.response.TuitionPackageResponse;
import com.apixenglish.center.modules.tuition.service.TuitionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
public class TuitionController {

    private final TuitionService tuitionService;

    @PostMapping("/api/v1/invoices")
    public ResponseEntity<ApiResponse<InvoiceResponse>> createInvoice(@Valid @RequestBody CreateInvoiceRequest request) {
        InvoiceResponse response = tuitionService.createInvoice(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Invoice created successfully"));
    }

    @GetMapping("/api/v1/invoices")
    public ResponseEntity<ApiResponse<List<InvoiceResponse>>> getInvoices(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UUID studentId,
            @RequestParam(required = false) UUID classId,
            @RequestParam(required = false) UUID campusId,
            @RequestParam(required = false) LocalDate billingMonth,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PageResponse<InvoiceResponse> pageResponse = tuitionService.getInvoices(
                search,
                studentId,
                classId,
                campusId,
                billingMonth,
                status,
                PageRequest.of(page, size)
        );
        return ResponseEntity.ok(ApiResponse.success(pageResponse, "Invoices retrieved successfully"));
    }

    @GetMapping("/api/v1/students/{studentId}/invoices")
    public ResponseEntity<ApiResponse<List<InvoiceResponse>>> getInvoicesByStudent(@PathVariable UUID studentId) {
        List<InvoiceResponse> response = tuitionService.getInvoicesByStudent(studentId);
        return ResponseEntity.ok(ApiResponse.success(response, "Student invoices retrieved successfully"));
    }

    @GetMapping("/api/v1/classes/{classId}/invoices")
    public ResponseEntity<ApiResponse<List<InvoiceResponse>>> getInvoicesByClass(@PathVariable UUID classId) {
        List<InvoiceResponse> response = tuitionService.getInvoicesByClass(classId);
        return ResponseEntity.ok(ApiResponse.success(response, "Class invoices retrieved successfully"));
    }

    @PostMapping("/api/v1/invoices/{invoiceId}/payments")
    public ResponseEntity<ApiResponse<InvoicePaymentResponse>> createPayment(
            @PathVariable UUID invoiceId,
            @Valid @RequestBody CreatePaymentRequest request
    ) {
        InvoicePaymentResponse response = tuitionService.createPayment(invoiceId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Payment registered successfully"));
    }

    @GetMapping("/api/v1/invoices/{invoiceId}/payments")
    public ResponseEntity<ApiResponse<List<InvoicePaymentResponse>>> getPaymentsByInvoice(@PathVariable UUID invoiceId) {
        List<InvoicePaymentResponse> response = tuitionService.getPaymentsByInvoice(invoiceId);
        return ResponseEntity.ok(ApiResponse.success(response, "Payments retrieved successfully"));
    }

    @GetMapping("/api/v1/tuition-packages")
    public ResponseEntity<ApiResponse<List<TuitionPackageResponse>>> getTuitionPackages() {
        return ResponseEntity.ok(ApiResponse.success(
                tuitionService.getActiveTuitionPackages(),
                "Tuition packages retrieved successfully"
        ));
    }

    @PostMapping("/api/v1/tuition-packages")
    public ResponseEntity<ApiResponse<TuitionPackageResponse>> createTuitionPackage(
            @Valid @RequestBody UpsertTuitionPackageRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                tuitionService.createTuitionPackage(request),
                "Tuition package created successfully"
        ));
    }

    @PutMapping("/api/v1/tuition-packages/{id}")
    public ResponseEntity<ApiResponse<TuitionPackageResponse>> updateTuitionPackage(
            @PathVariable UUID id,
            @Valid @RequestBody UpsertTuitionPackageRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                tuitionService.updateTuitionPackage(id, request),
                "Tuition package updated successfully"
        ));
    }

    @DeleteMapping("/api/v1/tuition-packages/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTuitionPackage(@PathVariable UUID id) {
        tuitionService.deleteTuitionPackage(id);
        return ResponseEntity.ok(ApiResponse.success("Tuition package deleted successfully"));
    }
}
