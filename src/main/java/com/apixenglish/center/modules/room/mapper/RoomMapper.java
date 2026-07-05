package com.apixenglish.center.modules.room.mapper;

import com.apixenglish.center.modules.room.dto.response.RoomResponse;
import com.apixenglish.center.modules.room.entity.Room;
import org.springframework.stereotype.Component;

@Component
public class RoomMapper {

    public RoomResponse toResponse(Room room) {
        if (room == null) {
            return null;
        }

        return RoomResponse.builder()
                .id(room.getId())
                .campusId(room.getCampus() != null ? room.getCampus().getId() : null)
                .campusName(room.getCampus() != null ? room.getCampus().getName() : null)
                .code(room.getCode())
                .name(room.getName())
                .capacity(room.getCapacity())
                .roomType(room.getRoomType())
                .facilitiesNote(room.getFacilitiesNote())
                .isActive(room.getIsActive())
                .build();
    }
}
