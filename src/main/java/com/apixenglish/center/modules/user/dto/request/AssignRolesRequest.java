package com.apixenglish.center.modules.user.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignRolesRequest {

    @NotEmpty(message = "Role IDs cannot be empty")
    private List<UUID> roleIds;

    private UUID campusId;

    private Instant expiredAt;
}
