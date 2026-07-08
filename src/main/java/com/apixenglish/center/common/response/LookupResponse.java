package com.apixenglish.center.common.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LookupResponse {
    private UUID id;
    private String code;
    private String name;
    private String displayName;
    private UUID parentId;
    private String parentName;
}
