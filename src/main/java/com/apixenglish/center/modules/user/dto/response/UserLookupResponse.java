package com.apixenglish.center.modules.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLookupResponse {
    private UUID id;
    private String fullName;
    private String email;
    private String phone;
    private String username;
    private String displayName;
}
