package com.apixenglish.center.modules.parent.controller;

import com.apixenglish.center.common.response.ApiResponse;
import com.apixenglish.center.common.response.PageResponse;
import com.apixenglish.center.modules.parent.dto.request.CreateParentRequest;
import com.apixenglish.center.modules.parent.dto.request.UpdateParentRequest;
import com.apixenglish.center.modules.parent.dto.response.ParentResponse;
import com.apixenglish.center.modules.parent.service.ParentService;
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
@RequestMapping("/api/v1/parents")
@RequiredArgsConstructor
public class ParentController {

    private final ParentService parentService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ParentResponse>>> getParents(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PageResponse<ParentResponse> pageResponse = parentService.getParents(search, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success(pageResponse, "Parents retrieved successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ParentResponse>> getParentById(@PathVariable UUID id) {
        ParentResponse response = parentService.getParentById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Parent retrieved successfully"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ParentResponse>> createParent(@Valid @RequestBody CreateParentRequest request) {
        ParentResponse response = parentService.createParent(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Parent created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ParentResponse>> updateParent(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateParentRequest request
    ) {
        ParentResponse response = parentService.updateParent(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Parent updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteParent(@PathVariable UUID id) {
        parentService.deleteParent(id);
        return ResponseEntity.ok(ApiResponse.success("Parent deleted successfully"));
    }
}
