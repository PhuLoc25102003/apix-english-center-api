package com.apixenglish.center.modules.parent.service;

import com.apixenglish.center.common.response.PageResponse;
import com.apixenglish.center.modules.parent.dto.request.CreateParentRequest;
import com.apixenglish.center.modules.parent.dto.request.UpdateParentRequest;
import com.apixenglish.center.modules.parent.dto.response.ParentResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ParentService {
    PageResponse<ParentResponse> getParents(String search, Pageable pageable);
    ParentResponse getParentById(UUID id);
    ParentResponse createParent(CreateParentRequest request);
    ParentResponse updateParent(UUID id, UpdateParentRequest request);
    void deleteParent(UUID id);
}
