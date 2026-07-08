package com.apixenglish.center.modules.curriculum.controller;

import com.apixenglish.center.common.response.ApiResponse;
import com.apixenglish.center.common.response.PageResponse;
import com.apixenglish.center.modules.curriculum.dto.request.CreateCurriculumRequest;
import com.apixenglish.center.modules.curriculum.dto.request.UpdateCurriculumRequest;
import com.apixenglish.center.modules.curriculum.dto.response.CurriculumLookupResponse;
import com.apixenglish.center.modules.curriculum.dto.response.CurriculumResponse;
import com.apixenglish.center.modules.curriculum.service.CurriculumService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/curriculums")
@RequiredArgsConstructor
public class CurriculumController {

    private final CurriculumService curriculumService;

    @GetMapping
    @PreAuthorize("hasAuthority('curriculum:read')")
    public ResponseEntity<ApiResponse<List<CurriculumResponse>>> getCurriculums(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UUID courseId,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort
    ) {
        String[] sortParams = sort.split(",");
        Sort sortOrder = Sort.by(sortParams[0]);
        if (sortParams.length > 1 && "desc".equalsIgnoreCase(sortParams[1])) {
            sortOrder = sortOrder.descending();
        }
        PageResponse<CurriculumResponse> pageResponse = curriculumService.listCurriculums(
                search, courseId, isActive, PageRequest.of(page, size, sortOrder));
        return ResponseEntity.ok(ApiResponse.success(pageResponse, "Curriculums retrieved successfully"));
    }

    @GetMapping("/lookup")
    @PreAuthorize("hasAuthority('curriculum:read')")
    public ResponseEntity<ApiResponse<List<CurriculumLookupResponse>>> lookupCurriculums() {
        List<CurriculumLookupResponse> list = curriculumService.lookupCurriculums();
        return ResponseEntity.ok(ApiResponse.success(list, "Curriculums lookup retrieved successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('curriculum:read')")
    public ResponseEntity<ApiResponse<CurriculumResponse>> getCurriculumById(@PathVariable UUID id) {
        CurriculumResponse response = curriculumService.getCurriculumById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Curriculum retrieved successfully"));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('curriculum:create')")
    public ResponseEntity<ApiResponse<CurriculumResponse>> createCurriculum(@Valid @RequestBody CreateCurriculumRequest request) {
        CurriculumResponse response = curriculumService.createCurriculum(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Curriculum created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('curriculum:update')")
    public ResponseEntity<ApiResponse<CurriculumResponse>> updateCurriculum(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCurriculumRequest request
    ) {
        CurriculumResponse response = curriculumService.updateCurriculum(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Curriculum updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('curriculum:delete')")
    public ResponseEntity<ApiResponse<Void>> deleteCurriculum(@PathVariable UUID id) {
        curriculumService.deleteCurriculum(id);
        return ResponseEntity.ok(ApiResponse.success("Curriculum deleted successfully"));
    }
}
