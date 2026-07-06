package com.apixenglish.center.modules.tuition.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class TuitionCalculatorTest {

    private final TuitionCalculator calculator = new TuitionCalculator();

    @Test
    void calculatesThreeMonthPercentageDiscount() {
        TuitionCalculator.TuitionCalculation result = calculator.calculate(
                new BigDecimal("1000000"),
                3,
                "PERCENTAGE",
                new BigDecimal("10")
        );

        assertThat(result.subtotalAmount()).isEqualByComparingTo("3000000");
        assertThat(result.discountAmount()).isEqualByComparingTo("300000");
        assertThat(result.totalAmount()).isEqualByComparingTo("2700000");
    }

    @Test
    void calculatesFixedAmountDiscount() {
        TuitionCalculator.TuitionCalculation result = calculator.calculate(
                new BigDecimal("1000000"),
                3,
                "FIXED_AMOUNT",
                new BigDecimal("250000")
        );

        assertThat(result.totalAmount()).isEqualByComparingTo("2750000");
    }

    @Test
    void calculatesOneMonthWithoutDiscount() {
        TuitionCalculator.TuitionCalculation result = calculator.calculate(
                new BigDecimal("1000000"),
                1,
                "NONE",
                BigDecimal.ZERO
        );

        assertThat(result.totalAmount()).isEqualByComparingTo("1000000");
    }
}
