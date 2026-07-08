package com.apixenglish.center.modules.position.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PositionResponse {
    private UUID id;
    private String code;
    private String name;
    private String description;
    private Boolean isTeachingPosition;
    private Boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;
}
