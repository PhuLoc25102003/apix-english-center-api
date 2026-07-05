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
public class CampusResponse {
    private UUID id;
    private String code;
    private String name;
    private String address;
    private String phone;
    private String description;
    private Boolean isActive;
}
