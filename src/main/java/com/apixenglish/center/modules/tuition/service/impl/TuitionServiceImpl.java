package com.apixenglish.center.modules.tuition.service.impl;

import com.apixenglish.center.common.exception.BusinessException;
import com.apixenglish.center.common.exception.ResourceNotFoundException;
import com.apixenglish.center.common.response.PageResponse;
import com.apixenglish.center.modules.classmanagement.entity.ClassEnrollment;
import com.apixenglish.center.modules.classmanagement.entity.Clazz;
import com.apixenglish.center.modules.classmanagement.repository.ClassEnrollmentRepository;
import com.apixenglish.center.modules.classmanagement.repository.ClazzRepository;
import com.apixenglish.center.modules.student.entity.Student;
import com.apixenglish.center.modules.student.repository.StudentRepository;
import com.apixenglish.center.modules.tuition.dto.request.CreateInvoiceRequest;
import com.apixenglish.center.modules.tuition.dto.request.CreatePaymentRequest;
import com.apixenglish.center.modules.tuition.dto.request.UpsertTuitionPackageRequest;
import com.apixenglish.center.modules.tuition.dto.response.InvoicePaymentResponse;
import com.apixenglish.center.modules.tuition.dto.response.InvoiceResponse;
import com.apixenglish.center.modules.tuition.dto.response.TuitionPackageResponse;
import com.apixenglish.center.modules.tuition.entity.Invoice;
import com.apixenglish.center.modules.tuition.entity.InvoicePayment;
import com.apixenglish.center.modules.tuition.entity.TuitionPackage;
import com.apixenglish.center.modules.tuition.mapper.InvoiceMapper;
import com.apixenglish.center.modules.tuition.mapper.InvoicePaymentMapper;
import com.apixenglish.center.modules.tuition.mapper.TuitionPackageMapper;
import com.apixenglish.center.modules.tuition.repository.InvoicePaymentRepository;
import com.apixenglish.center.modules.tuition.repository.InvoiceRepository;
import com.apixenglish.center.modules.tuition.repository.TuitionPackageRepository;
import com.apixenglish.center.modules.tuition.service.TuitionCalculator;
import com.apixenglish.center.modules.tuition.service.TuitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TuitionServiceImpl implements TuitionService {

    private final InvoiceRepository invoiceRepository;
    private final InvoicePaymentRepository invoicePaymentRepository;
    private final TuitionPackageRepository tuitionPackageRepository;
    private final StudentRepository studentRepository;
    private final ClazzRepository clazzRepository;
    private final ClassEnrollmentRepository classEnrollmentRepository;
    private final InvoiceMapper invoiceMapper;
    private final InvoicePaymentMapper invoicePaymentMapper;
    private final TuitionPackageMapper tuitionPackageMapper;
    private final TuitionCalculator tuitionCalculator;

    @Override
    @Transactional
    public InvoiceResponse createInvoice(CreateInvoiceRequest request) {
        Student student = studentRepository.findById(request.getStudentId())
                .filter(item -> item.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        Clazz clazz = clazzRepository.findById(request.getClassId())
                .filter(item -> item.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found"));
        ClassEnrollment enrollment = resolveEnrollment(request, student, clazz);
        TuitionPackage tuitionPackage = resolveTuitionPackage(request.getTuitionPackageId());

        if (tuitionPackage != null && !tuitionPackage.getNumberOfMonths().equals(request.getNumberOfMonths())) {
            throw new BusinessException(
                    "Number of months must match the selected tuition package",
                    "TUITION_PACKAGE_MONTHS_MISMATCH"
            );
        }

        BigDecimal monthlyFee = request.getMonthlyFee();
        if (monthlyFee == null && clazz.getCourse() != null) {
            monthlyFee = clazz.getCourse().getDefaultMonthlyTuitionFee();
        }
        if (monthlyFee == null || monthlyFee.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(
                    "A positive monthly tuition fee is required",
                    "MONTHLY_TUITION_FEE_REQUIRED"
            );
        }

        LocalDate billingStartMonth = request.getBillingStartMonth().withDayOfMonth(1);
        LocalDate billingEndMonth = billingStartMonth.plusMonths(request.getNumberOfMonths() - 1L);
        if (invoiceRepository.existsOverlappingBillingPeriod(
                student.getId(),
                clazz.getId(),
                billingStartMonth,
                billingEndMonth
        )) {
            throw new BusinessException(
                    "An invoice already covers part of this monthly billing period",
                    "DUPLICATE_BILLING_PERIOD"
            );
        }

        String discountType = tuitionPackage == null ? "NONE" : tuitionPackage.getDiscountType();
        BigDecimal discountValue = tuitionPackage == null
                ? BigDecimal.ZERO
                : tuitionPackage.getDiscountValue();
        TuitionCalculator.TuitionCalculation calculation = tuitionCalculator.calculate(
                monthlyFee,
                request.getNumberOfMonths(),
                discountType,
                discountValue
        );
        String periodLabel = billingStartMonth.format(DateTimeFormatter.ofPattern("MM/yyyy"));
        String title = request.getTitle() == null || request.getTitle().isBlank()
                ? "Monthly tuition " + periodLabel + " - " + clazz.getName()
                : request.getTitle().trim();

        Invoice invoice = Invoice.builder()
                .student(student)
                .clazz(clazz)
                .enrollment(enrollment)
                .tuitionPackage(tuitionPackage)
                .invoiceNo(generateNextInvoiceNo())
                .title(title)
                .description(request.getDescription())
                .billingStartMonth(billingStartMonth)
                .billingEndMonth(billingEndMonth)
                .numberOfMonths(request.getNumberOfMonths())
                .monthlyFee(monthlyFee)
                .subtotalAmount(calculation.subtotalAmount())
                .discountType(discountType)
                .discountValue(discountValue)
                .discountAmount(calculation.discountAmount())
                .totalAmount(calculation.totalAmount())
                .paidAmount(BigDecimal.ZERO)
                .remainingAmount(calculation.totalAmount())
                .dueDate(request.getDueDate())
                .status(request.getDueDate().isBefore(LocalDate.now()) ? "OVERDUE" : "UNPAID")
                .build();

        return invoiceMapper.toResponse(invoiceRepository.save(invoice));
    }

    @Override
    @Transactional
    public PageResponse<InvoiceResponse> getInvoices(
            String search,
            UUID studentId,
            UUID classId,
            UUID campusId,
            LocalDate billingMonth,
            String status,
            Pageable pageable
    ) {
        LocalDate normalizedMonth = billingMonth == null ? null : billingMonth.withDayOfMonth(1);
        Page<Invoice> invoicePage = invoiceRepository.searchInvoices(
                search,
                studentId,
                classId,
                campusId,
                normalizedMonth,
                status,
                pageable
        );
        invoicePage.forEach(this::refreshOverdueStatus);
        return PageResponse.of(invoicePage.map(invoiceMapper::toResponse));
    }

    @Override
    @Transactional
    public List<InvoiceResponse> getInvoicesByStudent(UUID studentId) {
        if (!studentRepository.existsById(studentId)) {
            throw new ResourceNotFoundException("Student not found");
        }
        return invoiceRepository.findByStudentIdAndDeletedAtIsNull(studentId).stream()
                .peek(this::refreshOverdueStatus)
                .map(invoiceMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public List<InvoiceResponse> getInvoicesByClass(UUID classId) {
        if (!clazzRepository.existsById(classId)) {
            throw new ResourceNotFoundException("Class not found");
        }
        return invoiceRepository.findByClazzIdAndDeletedAtIsNull(classId).stream()
                .peek(this::refreshOverdueStatus)
                .map(invoiceMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public InvoicePaymentResponse createPayment(UUID invoiceId, CreatePaymentRequest request) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .filter(item -> item.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));

        if ("CANCELLED".equals(invoice.getStatus()) || "REFUNDED".equals(invoice.getStatus())) {
            throw new BusinessException("Cannot pay this invoice", "INVOICE_NOT_PAYABLE");
        }
        if (request.getAmount().compareTo(invoice.getRemainingAmount()) > 0) {
            throw new BusinessException(
                    "Payment amount exceeds remaining amount",
                    "PAYMENT_AMOUNT_EXCEEDED"
            );
        }

        InvoicePayment payment = InvoicePayment.builder()
                .invoice(invoice)
                .paymentNo(generateNextPaymentNo())
                .amount(request.getAmount())
                .paymentDate(OffsetDateTime.now())
                .paymentMethod(request.getPaymentMethod())
                .note(request.getNote())
                .build();
        InvoicePayment savedPayment = invoicePaymentRepository.save(payment);

        BigDecimal newPaidAmount = invoice.getPaidAmount().add(request.getAmount());
        BigDecimal newRemainingAmount = invoice.getRemainingAmount().subtract(request.getAmount());
        invoice.setPaidAmount(newPaidAmount);
        invoice.setRemainingAmount(newRemainingAmount);
        if (newRemainingAmount.compareTo(BigDecimal.ZERO) == 0) {
            invoice.setStatus("PAID");
        } else if (invoice.getDueDate().isBefore(LocalDate.now())) {
            invoice.setStatus("OVERDUE");
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
        return invoicePaymentRepository.findByInvoiceIdAndDeletedAtIsNull(invoiceId).stream()
                .map(invoicePaymentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TuitionPackageResponse> getActiveTuitionPackages() {
        return tuitionPackageRepository.findByIsActiveTrueAndDeletedAtIsNullOrderByNumberOfMonthsAsc().stream()
                .map(tuitionPackageMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public TuitionPackageResponse createTuitionPackage(UpsertTuitionPackageRequest request) {
        validatePackage(request);
        TuitionPackage tuitionPackage = TuitionPackage.builder()
                .name(request.getName().trim())
                .numberOfMonths(request.getNumberOfMonths())
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .isActive(request.getIsActive())
                .description(request.getDescription())
                .build();
        return tuitionPackageMapper.toResponse(tuitionPackageRepository.save(tuitionPackage));
    }

    @Override
    @Transactional
    public TuitionPackageResponse updateTuitionPackage(UUID id, UpsertTuitionPackageRequest request) {
        validatePackage(request);
        TuitionPackage tuitionPackage = tuitionPackageRepository.findById(id)
                .filter(item -> item.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Tuition package not found"));
        tuitionPackage.setName(request.getName().trim());
        tuitionPackage.setNumberOfMonths(request.getNumberOfMonths());
        tuitionPackage.setDiscountType(request.getDiscountType());
        tuitionPackage.setDiscountValue(request.getDiscountValue());
        tuitionPackage.setIsActive(request.getIsActive());
        tuitionPackage.setDescription(request.getDescription());
        return tuitionPackageMapper.toResponse(tuitionPackageRepository.save(tuitionPackage));
    }

    @Override
    @Transactional
    public void deleteTuitionPackage(UUID id) {
        TuitionPackage tuitionPackage = tuitionPackageRepository.findById(id)
                .filter(item -> item.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Tuition package not found"));
        tuitionPackage.delete();
        tuitionPackageRepository.save(tuitionPackage);
    }

    private ClassEnrollment resolveEnrollment(
            CreateInvoiceRequest request,
            Student student,
            Clazz clazz
    ) {
        if (request.getEnrollmentId() == null) {
            return null;
        }
        ClassEnrollment enrollment = classEnrollmentRepository.findById(request.getEnrollmentId())
                .filter(item -> item.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found"));
        if (!enrollment.getStudent().getId().equals(student.getId())
                || !enrollment.getClazz().getId().equals(clazz.getId())) {
            throw new BusinessException(
                    "Enrollment does not belong to the selected student and class",
                    "INVALID_ENROLLMENT"
            );
        }
        return enrollment;
    }

    private TuitionPackage resolveTuitionPackage(UUID tuitionPackageId) {
        if (tuitionPackageId == null) {
            return null;
        }
        return tuitionPackageRepository.findById(tuitionPackageId)
                .filter(item -> item.getDeletedAt() == null && Boolean.TRUE.equals(item.getIsActive()))
                .orElseThrow(() -> new ResourceNotFoundException("Active tuition package not found"));
    }

    private void validatePackage(UpsertTuitionPackageRequest request) {
        if (!"PERCENTAGE".equals(request.getDiscountType())
                && !"FIXED_AMOUNT".equals(request.getDiscountType())
                && !"NONE".equals(request.getDiscountType())) {
            throw new BusinessException("Unsupported discount type", "INVALID_DISCOUNT_TYPE");
        }
        tuitionCalculator.calculate(
                BigDecimal.ONE,
                request.getNumberOfMonths(),
                request.getDiscountType(),
                request.getDiscountValue()
        );
    }

    private void refreshOverdueStatus(Invoice invoice) {
        if (invoice.getDueDate().isBefore(LocalDate.now())
                && invoice.getRemainingAmount().compareTo(BigDecimal.ZERO) > 0
                && !"CANCELLED".equals(invoice.getStatus())
                && !"REFUNDED".equals(invoice.getStatus())) {
            invoice.setStatus("OVERDUE");
        }
    }

    private synchronized String generateNextInvoiceNo() {
        String maxCode = invoiceRepository.findMaxInvoiceNo();
        if (maxCode == null) return "INV000001";
        try {
            return String.format("INV%06d", Integer.parseInt(maxCode.substring(3)) + 1);
        } catch (Exception exception) {
            return "INV" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        }
    }

    private synchronized String generateNextPaymentNo() {
        String maxCode = invoicePaymentRepository.findMaxPaymentNo();
        if (maxCode == null) return "PAY000001";
        try {
            return String.format("PAY%06d", Integer.parseInt(maxCode.substring(3)) + 1);
        } catch (Exception exception) {
            return "PAY" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        }
    }
}
