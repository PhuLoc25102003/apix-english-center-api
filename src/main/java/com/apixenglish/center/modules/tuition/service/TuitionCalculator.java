package com.apixenglish.center.modules.tuition.service;

import com.apixenglish.center.common.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class TuitionCalculator {

    public TuitionCalculation calculate(
            BigDecimal monthlyFee,
            int numberOfMonths,
            String discountType,
            BigDecimal discountValue
    ) {
        BigDecimal subtotal = monthlyFee.multiply(BigDecimal.valueOf(numberOfMonths));
        BigDecimal discount = BigDecimal.ZERO;
        BigDecimal value = discountValue == null ? BigDecimal.ZERO : discountValue;

        if ("PERCENTAGE".equals(discountType)) {
            if (value.compareTo(BigDecimal.valueOf(100)) > 0) {
                throw new BusinessException("Percentage discount cannot exceed 100", "INVALID_DISCOUNT");
            }
            discount = subtotal.multiply(value)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else if ("FIXED_AMOUNT".equals(discountType)) {
            discount = value.min(subtotal);
        } else if (discountType != null && !"NONE".equals(discountType)) {
            throw new BusinessException("Unsupported discount type", "INVALID_DISCOUNT_TYPE");
        }

        return new TuitionCalculation(subtotal, discount, subtotal.subtract(discount));
    }

    public record TuitionCalculation(
            BigDecimal subtotalAmount,
            BigDecimal discountAmount,
            BigDecimal totalAmount
    ) {
    }
}
