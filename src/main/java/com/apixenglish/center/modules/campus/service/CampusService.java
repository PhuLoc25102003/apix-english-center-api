package com.apixenglish.center.modules.campus.service;

import com.apixenglish.center.common.response.PageResponse;
import com.apixenglish.center.modules.campus.dto.request.CreateCampusRequest;
import com.apixenglish.center.modules.campus.dto.request.UpdateCampusRequest;
import com.apixenglish.center.modules.campus.dto.response.CampusResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CampusService {
    PageResponse<CampusResponse> getCampuses(String search, Pageable pageable);
    CampusResponse getCampusById(UUID id);
    CampusResponse createCampus(CreateCampusRequest request);
    CampusResponse updateCampus(UUID id, UpdateCampusRequest request);
    void deleteCampus(UUID id);
}
