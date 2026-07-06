package com.apixenglish.center.modules.tuition.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
public class CreateInvoiceRequest {

    @NotNull(message = "Student ID must not be null")
    private UUID studentId;

    @NotNull(message = "Class ID must not be null")
    private UUID classId;

    private UUID enrollmentId;

    @NotNull(message = "Billing start month must not be null")
    private LocalDate billingStartMonth;

    @NotNull(message = "Number of months must not be null")
    @Min(value = 1, message = "Number of months must be greater than 0")
    private Integer numberOfMonths;

    private UUID tuitionPackageId;

    @DecimalMin(value = "0.01", message = "Monthly fee must be greater than 0")
    private BigDecimal monthlyFee;

    private String title;

    private String description;

    @NotNull(message = "Due date must not be null")
    private LocalDate dueDate;
}
