package com.apixenglish.center.modules.campus.controller;

import com.apixenglish.center.common.response.ApiResponse;
import com.apixenglish.center.common.response.PageResponse;
import com.apixenglish.center.modules.campus.dto.request.CreateCampusRequest;
import com.apixenglish.center.modules.campus.dto.request.UpdateCampusRequest;
import com.apixenglish.center.modules.campus.dto.response.CampusResponse;
import com.apixenglish.center.modules.campus.dto.response.CampusLookupResponse;
import com.apixenglish.center.modules.campus.service.CampusService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/campuses")
@RequiredArgsConstructor
public class CampusController {

    private final CampusService campusService;

    @GetMapping("/lookup")
    @PreAuthorize("hasAuthority('campus:read')")
    public ResponseEntity<ApiResponse<List<CampusLookupResponse>>> lookupCampuses(
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "false") Boolean includeInactive
    ) {
        List<CampusLookupResponse> response = campusService.lookupCampuses(search, includeInactive);
        return ResponseEntity.ok(ApiResponse.success(response, "Campuses retrieved successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CampusResponse>>> getCampuses(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PageResponse<CampusResponse> pageResponse = campusService.getCampuses(search, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success(pageResponse, "Campuses retrieved successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CampusResponse>> getCampusById(@PathVariable UUID id) {
        CampusResponse response = campusService.getCampusById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Campus retrieved successfully"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CampusResponse>> createCampus(@Valid @RequestBody CreateCampusRequest request) {
        CampusResponse response = campusService.createCampus(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Campus created successfully"));
    }

    @RequestMapping(value = "/{id}", method = {RequestMethod.PUT, RequestMethod.PATCH})
    public ResponseEntity<ApiResponse<CampusResponse>> updateCampus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCampusRequest request
    ) {
        CampusResponse response = campusService.updateCampus(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Campus updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCampus(@PathVariable UUID id) {
        campusService.deleteCampus(id);
        return ResponseEntity.ok(ApiResponse.success("Campus deleted successfully"));
    }
}
