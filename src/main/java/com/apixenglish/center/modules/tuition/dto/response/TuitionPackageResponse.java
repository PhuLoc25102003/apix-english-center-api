package com.apixenglish.center.modules.tuition.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TuitionPackageResponse {
    private UUID id;
    private String name;
    private Integer numberOfMonths;
    private String discountType;
    private BigDecimal discountValue;
    private Boolean isActive;
    private String description;
}
