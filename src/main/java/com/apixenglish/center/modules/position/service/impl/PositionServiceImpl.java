package com.apixenglish.center.modules.position.service.impl;

import com.apixenglish.center.common.exception.BusinessException;
import com.apixenglish.center.common.exception.ResourceNotFoundException;
import com.apixenglish.center.common.response.PageResponse;
import com.apixenglish.center.modules.position.dto.request.CreatePositionRequest;
import com.apixenglish.center.modules.position.dto.request.UpdatePositionRequest;
import com.apixenglish.center.modules.position.dto.response.PositionLookupResponse;
import com.apixenglish.center.modules.position.dto.response.PositionResponse;
import com.apixenglish.center.modules.position.entity.Position;
import com.apixenglish.center.modules.position.mapper.PositionMapper;
import com.apixenglish.center.modules.position.repository.PositionRepository;
import com.apixenglish.center.modules.position.service.PositionService;
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
public class PositionServiceImpl implements PositionService {

    private final PositionRepository positionRepository;
    private final PositionMapper positionMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PositionResponse> listPositions(String search, Boolean isTeachingPosition, Boolean isActive, Pageable pageable) {
        Page<Position> page = positionRepository.searchPositions(search, isTeachingPosition, isActive, pageable);
        return PageResponse.of(page.map(positionMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PositionLookupResponse> lookupPositions() {
        return positionRepository.findAllActive().stream()
                .map(positionMapper::toLookupResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PositionResponse getPositionById(UUID id) {
        Position position = positionRepository.findById(id)
                .filter(p -> p.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Position not found"));
        return positionMapper.toResponse(position);
    }

    @Override
    @Transactional
    public PositionResponse createPosition(CreatePositionRequest request) {
        if (positionRepository.existsByCodeAndDeletedAtIsNull(request.getCode())) {
            throw new BusinessException("Position code already exists", "DUPLICATE_CODE");
        }
        Position position = Position.builder()
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .isTeachingPosition(request.getIsTeachingPosition())
                .isActive(request.getIsActive())
                .build();
        return positionMapper.toResponse(positionRepository.save(position));
    }

    @Override
    @Transactional
    public PositionResponse updatePosition(UUID id, UpdatePositionRequest request) {
        Position position = positionRepository.findById(id)
                .filter(p -> p.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Position not found"));
        position.setName(request.getName());
        position.setDescription(request.getDescription());
        position.setIsTeachingPosition(request.getIsTeachingPosition());
        position.setIsActive(request.getIsActive());
        return positionMapper.toResponse(positionRepository.save(position));
    }

    @Override
    @Transactional
    public void deletePosition(UUID id) {
        Position position = positionRepository.findById(id)
                .filter(p -> p.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Position not found"));
        position.delete();
        positionRepository.save(position);
    }
}
