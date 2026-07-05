package com.apixenglish.center.modules.classmanagement.service;

import com.apixenglish.center.common.response.PageResponse;
import com.apixenglish.center.modules.classmanagement.dto.request.CreateClassRequest;
import com.apixenglish.center.modules.classmanagement.dto.request.UpdateClassRequest;
import com.apixenglish.center.modules.classmanagement.dto.response.ClassResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ClazzService {
    PageResponse<ClassResponse> getClasses(String search, Pageable pageable);
    ClassResponse getClassById(UUID id);
    ClassResponse createClass(CreateClassRequest request);
    ClassResponse updateClass(UUID id, UpdateClassRequest request);
    void deleteClass(UUID id);
}
