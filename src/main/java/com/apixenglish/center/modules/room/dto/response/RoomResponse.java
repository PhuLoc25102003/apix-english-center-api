package com.apixenglish.center.modules.room.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomResponse {
    private UUID id;
    private UUID campusId;
    private String campusName;
    private String code;
    private String name;
    private Integer capacity;
    private String roomType;
    private String facilitiesNote;
    private Boolean isActive;
}
