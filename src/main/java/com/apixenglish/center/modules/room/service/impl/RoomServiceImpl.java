package com.apixenglish.center.modules.room.service.impl;

import com.apixenglish.center.common.exception.ConflictException;
import com.apixenglish.center.common.exception.ResourceNotFoundException;
import com.apixenglish.center.common.response.PageResponse;
import com.apixenglish.center.modules.campus.entity.Campus;
import com.apixenglish.center.modules.campus.repository.CampusRepository;
import com.apixenglish.center.modules.room.dto.request.CreateRoomRequest;
import com.apixenglish.center.modules.room.dto.request.UpdateRoomRequest;
import com.apixenglish.center.modules.room.dto.response.RoomResponse;
import com.apixenglish.center.modules.room.entity.Room;
import com.apixenglish.center.modules.room.mapper.RoomMapper;
import com.apixenglish.center.modules.room.repository.RoomRepository;
import com.apixenglish.center.modules.room.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final CampusRepository campusRepository;
    private final RoomMapper roomMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<RoomResponse> getRooms(String search, Pageable pageable) {
        Page<Room> roomPage = roomRepository.searchRooms(search, pageable);
        Page<RoomResponse> responsePage = roomPage.map(roomMapper::toResponse);
        return PageResponse.of(responsePage);
    }

    @Override
    @Transactional(readOnly = true)
    public RoomResponse getRoomById(UUID id) {
        Room room = roomRepository.findById(id)
                .filter(r -> r.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));
        return roomMapper.toResponse(room);
    }

    @Override
    @Transactional
    public RoomResponse createRoom(CreateRoomRequest request) {
        Campus campus = campusRepository.findById(request.getCampusId())
                .filter(c -> c.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Campus not found"));

        if (roomRepository.existsByCampusIdAndCodeAndDeletedAtIsNull(request.getCampusId(), request.getCode())) {
            throw new ConflictException("Room code already exists in this campus");
        }

        Room room = Room.builder()
                .campus(campus)
                .code(request.getCode())
                .name(request.getName())
                .capacity(request.getCapacity())
                .roomType(request.getRoomType())
                .facilitiesNote(request.getFacilitiesNote())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        Room savedRoom = roomRepository.save(room);
        return roomMapper.toResponse(savedRoom);
    }

    @Override
    @Transactional
    public RoomResponse updateRoom(UUID id, UpdateRoomRequest request) {
        Room room = roomRepository.findById(id)
                .filter(r -> r.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        Campus campus = campusRepository.findById(request.getCampusId())
                .filter(c -> c.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Campus not found"));

        if (!room.getCampus().getId().equals(request.getCampusId()) || !room.getCode().equals(request.getCode())) {
            if (roomRepository.existsByCampusIdAndCodeAndDeletedAtIsNull(request.getCampusId(), request.getCode())) {
                throw new ConflictException("Room code already exists in this campus");
            }
        }

        room.setCampus(campus);
        room.setCode(request.getCode());
        room.setName(request.getName());
        room.setCapacity(request.getCapacity());
        room.setRoomType(request.getRoomType());
        room.setFacilitiesNote(request.getFacilitiesNote());
        room.setIsActive(request.getIsActive());

        Room updatedRoom = roomRepository.save(room);
        return roomMapper.toResponse(updatedRoom);
    }

    @Override
    @Transactional
    public void deleteRoom(UUID id) {
        Room room = roomRepository.findById(id)
                .filter(r -> r.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        room.delete();
        roomRepository.save(room);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomResponse> getRoomsByCampusId(UUID campusId) {
        if (!campusRepository.existsById(campusId)) {
            throw new ResourceNotFoundException("Campus not found");
        }
        List<Room> rooms = roomRepository.findByCampusIdAndDeletedAtIsNull(campusId);
        return rooms.stream()
                .map(roomMapper::toResponse)
                .collect(Collectors.toList());
    }
}
