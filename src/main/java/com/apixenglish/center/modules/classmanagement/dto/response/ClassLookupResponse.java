package com.apixenglish.center.modules.classmanagement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassLookupResponse {
    private UUID id;
    private String classCode;
    private String name;
    private String displayName;
}
