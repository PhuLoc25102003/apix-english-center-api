package com.apixenglish.center.modules.position.service;

import com.apixenglish.center.common.response.PageResponse;
import com.apixenglish.center.modules.position.dto.request.CreatePositionRequest;
import com.apixenglish.center.modules.position.dto.request.UpdatePositionRequest;
import com.apixenglish.center.modules.position.dto.response.PositionLookupResponse;
import com.apixenglish.center.modules.position.dto.response.PositionResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface PositionService {
    PageResponse<PositionResponse> listPositions(String search, Boolean isTeachingPosition, Boolean isActive, Pageable pageable);
    List<PositionLookupResponse> lookupPositions();
    PositionResponse getPositionById(UUID id);
    PositionResponse createPosition(CreatePositionRequest request);
    PositionResponse updatePosition(UUID id, UpdatePositionRequest request);
    void deletePosition(UUID id);
}
