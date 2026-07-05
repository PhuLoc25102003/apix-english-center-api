package com.apixenglish.center.modules.tuition.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
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

    @NotBlank(message = "Title must not be blank")
    private String title;

    private String description;

    @NotNull(message = "Total amount must not be null")
    @DecimalMin(value = "0.0", message = "Total amount must not be negative")
    private BigDecimal totalAmount;

    @NotNull(message = "Due date must not be null")
    private LocalDate dueDate;
}
