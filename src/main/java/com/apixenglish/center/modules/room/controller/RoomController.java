package com.apixenglish.center.modules.room.controller;

import com.apixenglish.center.common.response.ApiResponse;
import com.apixenglish.center.common.response.PageResponse;
import com.apixenglish.center.modules.room.dto.request.CreateRoomRequest;
import com.apixenglish.center.modules.room.dto.request.UpdateRoomRequest;
import com.apixenglish.center.modules.room.dto.response.RoomResponse;
import com.apixenglish.center.modules.room.service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @GetMapping("/api/v1/rooms")
    public ResponseEntity<ApiResponse<List<RoomResponse>>> getRooms(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PageResponse<RoomResponse> pageResponse = roomService.getRooms(search, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success(pageResponse, "Rooms retrieved successfully"));
    }

    @GetMapping("/api/v1/rooms/{id}")
    public ResponseEntity<ApiResponse<RoomResponse>> getRoomById(@PathVariable UUID id) {
        RoomResponse response = roomService.getRoomById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Room retrieved successfully"));
    }

    @PostMapping("/api/v1/rooms")
    public ResponseEntity<ApiResponse<RoomResponse>> createRoom(@Valid @RequestBody CreateRoomRequest request) {
        RoomResponse response = roomService.createRoom(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Room created successfully"));
    }

    @PutMapping("/api/v1/rooms/{id}")
    public ResponseEntity<ApiResponse<RoomResponse>> updateRoom(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateRoomRequest request
    ) {
        RoomResponse response = roomService.updateRoom(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Room updated successfully"));
    }

    @DeleteMapping("/api/v1/rooms/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRoom(@PathVariable UUID id) {
        roomService.deleteRoom(id);
        return ResponseEntity.ok(ApiResponse.success("Room deleted successfully"));
    }

    @GetMapping("/api/v1/campuses/{campusId}/rooms")
    public ResponseEntity<ApiResponse<List<RoomResponse>>> getRoomsByCampusId(@PathVariable UUID campusId) {
        List<RoomResponse> response = roomService.getRoomsByCampusId(campusId);
        return ResponseEntity.ok(ApiResponse.success(response, "Campus rooms retrieved successfully"));
    }
}
