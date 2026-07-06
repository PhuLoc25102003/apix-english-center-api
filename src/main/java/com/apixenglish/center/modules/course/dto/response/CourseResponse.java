package com.apixenglish.center.modules.course.dto.response;

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
public class CourseResponse {
    private UUID id;
    private UUID levelId;
    private String levelName;
    private String code;
    private String name;
    private String description;
    private Integer totalLessons;
    private Integer durationMinutes;
    private BigDecimal defaultMonthlyTuitionFee;
    private String status;
}
