package com.apixenglish.center.modules.curriculum.service;

import com.apixenglish.center.common.response.PageResponse;
import com.apixenglish.center.modules.curriculum.dto.request.CreateCurriculumRequest;
import com.apixenglish.center.modules.curriculum.dto.request.UpdateCurriculumRequest;
import com.apixenglish.center.modules.curriculum.dto.response.CurriculumLookupResponse;
import com.apixenglish.center.modules.curriculum.dto.response.CurriculumResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface CurriculumService {
    PageResponse<CurriculumResponse> listCurriculums(String search, UUID courseId, Boolean isActive, Pageable pageable);
    List<CurriculumLookupResponse> lookupCurriculums();
    CurriculumResponse getCurriculumById(UUID id);
    CurriculumResponse createCurriculum(CreateCurriculumRequest request);
    CurriculumResponse updateCurriculum(UUID id, UpdateCurriculumRequest request);
    void deleteCurriculum(UUID id);
}
