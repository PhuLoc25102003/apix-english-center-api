package com.apixenglish.center.modules.campus.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCampusRequest {

    @NotBlank(message = "Campus code must not be blank")
    private String code;

    @NotBlank(message = "Campus name must not be blank")
    private String name;

    private String address;
    private String phone;
    private String description;
    private Boolean isActive;
}
