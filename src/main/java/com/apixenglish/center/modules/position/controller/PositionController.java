package com.apixenglish.center.modules.position.controller;

import com.apixenglish.center.common.response.ApiResponse;
import com.apixenglish.center.common.response.PageResponse;
import com.apixenglish.center.modules.position.dto.request.CreatePositionRequest;
import com.apixenglish.center.modules.position.dto.request.UpdatePositionRequest;
import com.apixenglish.center.modules.position.dto.response.PositionLookupResponse;
import com.apixenglish.center.modules.position.dto.response.PositionResponse;
import com.apixenglish.center.modules.position.service.PositionService;
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
@RequestMapping("/api/v1/positions")
@RequiredArgsConstructor
public class PositionController {

    private final PositionService positionService;

    @GetMapping
    @PreAuthorize("hasAuthority('position:read')")
    public ResponseEntity<ApiResponse<List<PositionResponse>>> getPositions(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isTeachingPosition,
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
        PageResponse<PositionResponse> pageResponse = positionService.listPositions(
                search, isTeachingPosition, isActive, PageRequest.of(page, size, sortOrder));
        return ResponseEntity.ok(ApiResponse.success(pageResponse, "Positions retrieved successfully"));
    }

    @GetMapping("/lookup")
    @PreAuthorize("hasAnyAuthority('position:read', 'employee:create', 'employee:update')")
    public ResponseEntity<ApiResponse<List<PositionLookupResponse>>> lookupPositions() {
        List<PositionLookupResponse> list = positionService.lookupPositions();
        return ResponseEntity.ok(ApiResponse.success(list, "Positions retrieved successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('position:read')")
    public ResponseEntity<ApiResponse<PositionResponse>> getPositionById(@PathVariable UUID id) {
        PositionResponse response = positionService.getPositionById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Position retrieved successfully"));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('position:create')")
    public ResponseEntity<ApiResponse<PositionResponse>> createPosition(@Valid @RequestBody CreatePositionRequest request) {
        PositionResponse response = positionService.createPosition(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Position created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('position:update')")
    public ResponseEntity<ApiResponse<PositionResponse>> updatePosition(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePositionRequest request
    ) {
        PositionResponse response = positionService.updatePosition(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Position updated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('position:delete')")
    public ResponseEntity<ApiResponse<Void>> deletePosition(@PathVariable UUID id) {
        positionService.deletePosition(id);
        return ResponseEntity.ok(ApiResponse.success("Position deleted successfully"));
    }
}
