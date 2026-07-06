package com.apixenglish.center.modules.user.dto.request;

import com.apixenglish.center.modules.user.entity.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @Email(message = "Email must be valid")
    private String email;

    private String phone;

    private String username;

    private String avatarUrl;

    private UserStatus status;

    private Boolean emailVerified;

    private Boolean phoneVerified;

    private Boolean mfaEnabled;
}
