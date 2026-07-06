package com.apixenglish.center.modules.tuition.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpsertTuitionPackageRequest {

    @NotBlank(message = "Package name must not be blank")
    private String name;

    @NotNull(message = "Number of months must not be null")
    @Min(value = 1, message = "Number of months must be greater than 0")
    private Integer numberOfMonths;

    @NotBlank(message = "Discount type must not be blank")
    private String discountType;

    @NotNull(message = "Discount value must not be null")
    @DecimalMin(value = "0.0", message = "Discount value must not be negative")
    private BigDecimal discountValue;

    @NotNull(message = "Active status must not be null")
    private Boolean isActive;

    private String description;
}
