package com.apixenglish.center.modules.room.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRoomRequest {

    @NotNull(message = "Campus ID must not be null")
    private UUID campusId;

    @NotBlank(message = "Room code must not be blank")
    private String code;

    @NotBlank(message = "Room name must not be blank")
    private String name;

    @NotNull(message = "Capacity must not be null")
    @Min(value = 1, message = "Capacity must be greater than 0")
    private Integer capacity;

    private String roomType;
    private String facilitiesNote;

    @NotNull(message = "Active status must not be null")
    private Boolean isActive;
}
