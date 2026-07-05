package com.apixenglish.center.modules.course.service;

import com.apixenglish.center.common.response.PageResponse;
import com.apixenglish.center.modules.course.dto.request.CreateLevelRequest;
import com.apixenglish.center.modules.course.dto.request.UpdateLevelRequest;
import com.apixenglish.center.modules.course.dto.response.LevelResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface LevelService {
    PageResponse<LevelResponse> getLevels(String search, Pageable pageable);
    LevelResponse getLevelById(UUID id);
    LevelResponse createLevel(CreateLevelRequest request);
    LevelResponse updateLevel(UUID id, UpdateLevelRequest request);
    void deleteLevel(UUID id);
}
