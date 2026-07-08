package com.apixenglish.center.modules.course.controller;

import com.apixenglish.center.common.response.ApiResponse;
import com.apixenglish.center.common.response.PageResponse;
import com.apixenglish.center.common.response.LookupResponse;
import com.apixenglish.center.modules.course.repository.LevelRepository;
import com.apixenglish.center.modules.course.dto.request.CreateLevelRequest;
import com.apixenglish.center.modules.course.dto.request.UpdateLevelRequest;
import com.apixenglish.center.modules.course.dto.response.LevelResponse;
import com.apixenglish.center.modules.course.service.LevelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/levels")
@RequiredArgsConstructor
public class LevelController {

    private final LevelService levelService;
    private final LevelRepository levelRepository;

    @GetMapping("/lookup")
    public ResponseEntity<ApiResponse<List<LookupResponse>>> lookupLevels() {
        List<LookupResponse> levels = levelRepository.findByDeletedAtIsNullAndIsActiveTrueOrderByOrderIndexAsc().stream()
                .map(level -> LookupResponse.builder().id(level.getId()).code(level.getCode()).name(level.getName())
                        .displayName(level.getCode() + " - " + level.getName()).build())
                .toList();
        return ResponseEntity.ok(ApiResponse.success(levels, "Levels lookup retrieved successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<LevelResponse>>> getLevels(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PageResponse<LevelResponse> pageResponse = levelService.getLevels(search, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success(pageResponse, "Levels retrieved successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LevelResponse>> getLevelById(@PathVariable UUID id) {
        LevelResponse response = levelService.getLevelById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Level retrieved successfully"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<LevelResponse>> createLevel(@Valid @RequestBody CreateLevelRequest request) {
        LevelResponse response = levelService.createLevel(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Level created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<LevelResponse>> updateLevel(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateLevelRequest request
    ) {
        LevelResponse response = levelService.updateLevel(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Level updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteLevel(@PathVariable UUID id) {
        levelService.deleteLevel(id);
        return ResponseEntity.ok(ApiResponse.success("Level deleted successfully"));
    }
}
