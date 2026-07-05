package com.apixenglish.center.modules.course.service.impl;

import com.apixenglish.center.common.exception.ConflictException;
import com.apixenglish.center.common.exception.ResourceNotFoundException;
import com.apixenglish.center.common.response.PageResponse;
import com.apixenglish.center.modules.course.dto.request.CreateLevelRequest;
import com.apixenglish.center.modules.course.dto.request.UpdateLevelRequest;
import com.apixenglish.center.modules.course.dto.response.LevelResponse;
import com.apixenglish.center.modules.course.entity.Level;
import com.apixenglish.center.modules.course.mapper.LevelMapper;
import com.apixenglish.center.modules.course.repository.LevelRepository;
import com.apixenglish.center.modules.course.service.LevelService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LevelServiceImpl implements LevelService {

    private final LevelRepository levelRepository;
    private final LevelMapper levelMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<LevelResponse> getLevels(String search, Pageable pageable) {
        Page<Level> levelPage = levelRepository.searchLevels(search, pageable);
        Page<LevelResponse> responsePage = levelPage.map(levelMapper::toResponse);
        return PageResponse.of(responsePage);
    }

    @Override
    @Transactional(readOnly = true)
    public LevelResponse getLevelById(UUID id) {
        Level level = levelRepository.findById(id)
                .filter(l -> l.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Level not found"));
        return levelMapper.toResponse(level);
    }

    @Override
    @Transactional
    public LevelResponse createLevel(CreateLevelRequest request) {
        if (levelRepository.existsByCodeAndDeletedAtIsNull(request.getCode())) {
            throw new ConflictException("Level code already exists");
        }

        Level level = Level.builder()
                .code(request.getCode())
                .name(request.getName())
                .orderIndex(request.getOrderIndex())
                .description(request.getDescription())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        Level savedLevel = levelRepository.save(level);
        return levelMapper.toResponse(savedLevel);
    }

    @Override
    @Transactional
    public LevelResponse updateLevel(UUID id, UpdateLevelRequest request) {
        Level level = levelRepository.findById(id)
                .filter(l -> l.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Level not found"));

        if (!level.getCode().equals(request.getCode())) {
            if (levelRepository.existsByCodeAndDeletedAtIsNull(request.getCode())) {
                throw new ConflictException("Level code already exists");
            }
        }

        level.setCode(request.getCode());
        level.setName(request.getName());
        level.setOrderIndex(request.getOrderIndex());
        level.setDescription(request.getDescription());
        level.setIsActive(request.getIsActive());

        Level updatedLevel = levelRepository.save(level);
        return levelMapper.toResponse(updatedLevel);
    }

    @Override
    @Transactional
    public void deleteLevel(UUID id) {
        Level level = levelRepository.findById(id)
                .filter(l -> l.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Level not found"));

        level.delete();
        levelRepository.save(level);
    }
}
