package com.apixenglish.center.modules.campus.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCampusRequest {

    @NotBlank(message = "Campus code must not be blank")
    private String code;

    @NotBlank(message = "Campus name must not be blank")
    private String name;

    private String address;
    private String phone;
    private String description;

    @NotNull(message = "Active status must not be null")
    private Boolean isActive;
}
