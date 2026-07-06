package com.apixenglish.center.modules.tuition.mapper;

import com.apixenglish.center.modules.tuition.dto.response.TuitionPackageResponse;
import com.apixenglish.center.modules.tuition.entity.TuitionPackage;
import org.springframework.stereotype.Component;

@Component
public class TuitionPackageMapper {

    public TuitionPackageResponse toResponse(TuitionPackage tuitionPackage) {
        return TuitionPackageResponse.builder()
                .id(tuitionPackage.getId())
                .name(tuitionPackage.getName())
                .numberOfMonths(tuitionPackage.getNumberOfMonths())
                .discountType(tuitionPackage.getDiscountType())
                .discountValue(tuitionPackage.getDiscountValue())
                .isActive(tuitionPackage.getIsActive())
                .description(tuitionPackage.getDescription())
                .build();
    }
}
