package com.apixenglish.center.modules.classmanagement.service;

import com.apixenglish.center.common.response.PageResponse;
import com.apixenglish.center.modules.classmanagement.dto.request.CreateClassRequest;
import com.apixenglish.center.modules.classmanagement.dto.request.UpdateClassRequest;
import com.apixenglish.center.modules.classmanagement.dto.response.ClassResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

import com.apixenglish.center.modules.classmanagement.dto.response.ClassLookupResponse;
import java.util.List;

public interface ClazzService {
    PageResponse<ClassResponse> getClasses(String search, UUID teacherId, Pageable pageable);
    List<ClassLookupResponse> lookupClasses();
    ClassResponse getClassById(UUID id);
    ClassResponse createClass(CreateClassRequest request);
    ClassResponse updateClass(UUID id, UpdateClassRequest request);
    void deleteClass(UUID id);
}
