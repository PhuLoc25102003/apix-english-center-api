package com.apixenglish.center.modules.parent.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParentResponse {
    private UUID id;
    private UUID userId;
    private String parentCode;
    private String fullName;
    private String phone;
    private String email;
    private String address;
    private String jobTitle;
    private String note;
}
