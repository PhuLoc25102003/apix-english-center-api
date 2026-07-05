package com.apixenglish.center.modules.room.service;

import com.apixenglish.center.common.response.PageResponse;
import com.apixenglish.center.modules.room.dto.request.CreateRoomRequest;
import com.apixenglish.center.modules.room.dto.request.UpdateRoomRequest;
import com.apixenglish.center.modules.room.dto.response.RoomResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface RoomService {
    PageResponse<RoomResponse> getRooms(String search, Pageable pageable);
    RoomResponse getRoomById(UUID id);
    RoomResponse createRoom(CreateRoomRequest request);
    RoomResponse updateRoom(UUID id, UpdateRoomRequest request);
    void deleteRoom(UUID id);
    List<RoomResponse> getRoomsByCampusId(UUID campusId);
}
