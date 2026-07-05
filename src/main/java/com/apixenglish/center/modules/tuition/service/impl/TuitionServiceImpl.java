package com.apixenglish.center.modules.tuition.service.impl;

import com.apixenglish.center.common.exception.BusinessException;
import com.apixenglish.center.common.exception.ResourceNotFoundException;
import com.apixenglish.center.common.response.PageResponse;
import com.apixenglish.center.modules.student.entity.Student;
import com.apixenglish.center.modules.student.repository.StudentRepository;
import com.apixenglish.center.modules.tuition.dto.request.CreateInvoiceRequest;
import com.apixenglish.center.modules.tuition.dto.request.CreatePaymentRequest;
import com.apixenglish.center.modules.tuition.dto.response.InvoicePaymentResponse;
import com.apixenglish.center.modules.tuition.dto.response.InvoiceResponse;
import com.apixenglish.center.modules.tuition.entity.Invoice;
import com.apixenglish.center.modules.tuition.entity.InvoicePayment;
import com.apixenglish.center.modules.tuition.mapper.InvoiceMapper;
import com.apixenglish.center.modules.tuition.mapper.InvoicePaymentMapper;
import com.apixenglish.center.modules.tuition.repository.InvoicePaymentRepository;
import com.apixenglish.center.modules.tuition.repository.InvoiceRepository;
import com.apixenglish.center.modules.tuition.service.TuitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TuitionServiceImpl implements TuitionService {

    private final InvoiceRepository invoiceRepository;
    private final InvoicePaymentRepository invoicePaymentRepository;
    private final StudentRepository studentRepository;
    private final InvoiceMapper invoiceMapper;
    private final InvoicePaymentMapper invoicePaymentMapper;

    @Override
    @Transactional
    public InvoiceResponse createInvoice(CreateInvoiceRequest request) {
        Student student = studentRepository.findById(request.getStudentId())
                .filter(s -> s.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        String invoiceNo = generateNextInvoiceNo();

        Invoice invoice = Invoice.builder()
                .student(student)
                .invoiceNo(invoiceNo)
                .title(request.getTitle())
                .description(request.getDescription())
                .totalAmount(request.getTotalAmount())
                .paidAmount(BigDecimal.ZERO)
                .remainingAmount(request.getTotalAmount())
                .dueDate(request.getDueDate())
                .status("UNPAID")
                .build();

        Invoice saved = invoiceRepository.save(invoice);
        return invoiceMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<InvoiceResponse> getInvoices(String search, Pageable pageable) {
        Page<Invoice> invoicePage = invoiceRepository.searchInvoices(search, pageable);
        Page<InvoiceResponse> responsePage = invoicePage.map(invoiceMapper::toResponse);
        return PageResponse.of(responsePage);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceResponse> getInvoicesByStudent(UUID studentId) {
        if (!studentRepository.existsById(studentId)) {
            throw new ResourceNotFoundException("Student not found");
        }
        return invoiceRepository.findByStudentIdAndDeletedAtIsNull(studentId)
                .stream()
                .map(invoiceMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public InvoicePaymentResponse createPayment(UUID invoiceId, CreatePaymentRequest request) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .filter(i -> i.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));

        if (invoice.getStatus().equals("CANCELLED")) {
            throw new BusinessException("Cannot pay cancelled invoice", "INVOICE_CANCELLED");
        }

        if (request.getAmount().compareTo(invoice.getRemainingAmount()) > 0) {
            throw new BusinessException("Payment amount exceeds remaining amount", "PAYMENT_AMOUNT_EXCEEDED");
        }

        String paymentNo = generateNextPaymentNo();

        InvoicePayment payment = InvoicePayment.builder()
                .invoice(invoice)
                .paymentNo(paymentNo)
                .amount(request.getAmount())
                .paymentDate(OffsetDateTime.now())
                .paymentMethod(request.getPaymentMethod())
                .note(request.getNote())
                .build();

        InvoicePayment savedPayment = invoicePaymentRepository.save(payment);

        // Update amounts
        BigDecimal newPaidAmount = invoice.getPaidAmount().add(request.getAmount());
        BigDecimal newRemainingAmount = invoice.getRemainingAmount().subtract(request.getAmount());

        invoice.setPaidAmount(newPaidAmount);
        invoice.setRemainingAmount(newRemainingAmount);

        // Update status
        if (newRemainingAmount.compareTo(BigDecimal.ZERO) == 0) {
            invoice.setStatus("PAID");
        } else {
            invoice.setStatus("PARTIALLY_PAID");
        }

        invoiceRepository.save(invoice);

        return invoicePaymentMapper.toResponse(savedPayment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoicePaymentResponse> getPaymentsByInvoice(UUID invoiceId) {
        if (!invoiceRepository.existsById(invoiceId)) {
            throw new ResourceNotFoundException("Invoice not found");
        }
        return invoicePaymentRepository.findByInvoiceIdAndDeletedAtIsNull(invoiceId)
                .stream()
                .map(invoicePaymentMapper::toResponse)
                .collect(Collectors.toList());
    }

    private synchronized String generateNextInvoiceNo() {
        String maxCode = invoiceRepository.findMaxInvoiceNo();
        if (maxCode == null) {
            return "INV000001";
        }
        try {
            int numericPart = Integer.parseInt(maxCode.substring(3));
            return String.format("INV%06d", numericPart + 1);
        } catch (Exception e) {
            return "INV" + java.util.UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        }
    }

    private synchronized String generateNextPaymentNo() {
        String maxCode = invoicePaymentRepository.findMaxPaymentNo();
        if (maxCode == null) {
            return "PAY000001";
        }
        try {
            int numericPart = Integer.parseInt(maxCode.substring(3));
            return String.format("PAY%06d", numericPart + 1);
        } catch (Exception e) {
            return "PAY" + java.util.UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        }
    }
}
