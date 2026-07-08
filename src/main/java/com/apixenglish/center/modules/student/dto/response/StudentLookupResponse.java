package com.apixenglish.center.modules.student.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentLookupResponse {
    private UUID id;
    private String studentCode;
    private String fullName;
    private String displayName;
}
