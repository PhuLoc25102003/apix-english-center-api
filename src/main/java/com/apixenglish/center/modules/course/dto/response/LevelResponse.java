package com.apixenglish.center.modules.course.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LevelResponse {
    private UUID id;
    private String code;
    private String name;
    private Integer orderIndex;
    private String description;
    private Boolean isActive;
}
