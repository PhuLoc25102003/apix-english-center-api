package com.apixenglish.center.modules.campus.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampusLookupResponse {
    private UUID id;
    private String code;
    private String name;
    private String displayName;
}
