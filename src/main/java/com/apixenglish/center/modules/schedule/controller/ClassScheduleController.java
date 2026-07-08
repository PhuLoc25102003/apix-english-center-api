package com.apixenglish.center.modules.schedule.controller;

import com.apixenglish.center.common.response.ApiResponse;
import com.apixenglish.center.common.response.PageResponse;
import com.apixenglish.center.modules.schedule.dto.request.CreateClassScheduleRequest;
import com.apixenglish.center.modules.schedule.dto.request.CreateSchedulePatternRequest;
import com.apixenglish.center.modules.schedule.dto.response.ClassScheduleResponse;
import com.apixenglish.center.modules.schedule.dto.response.SchedulePatternResponse;
import com.apixenglish.center.modules.schedule.service.ClassScheduleService;
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
@RequestMapping("/api/v1/class-schedules")
@RequiredArgsConstructor
public class ClassScheduleController {

    private final ClassScheduleService classScheduleService;

    @GetMapping
    @PreAuthorize("hasAuthority('class-schedule:read')")
    public ResponseEntity<ApiResponse<List<ClassScheduleResponse>>> getSchedules(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UUID classId,
            @RequestParam(required = false) UUID teacherId,
            @RequestParam(required = false) UUID roomId,
            @RequestParam(required = false) UUID campusId,
            @RequestParam(required = false) String schedulePattern,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort
    ) {
        String[] sortParams = sort.split(",");
        Sort sortOrder = Sort.by(sortParams[0]);
        if (sortParams.length > 1 && "desc".equalsIgnoreCase(sortParams[1])) {
            sortOrder = sortOrder.descending();
        }
        PageResponse<ClassScheduleResponse> pageResponse = classScheduleService.listSchedules(
                search, classId, teacherId, roomId, campusId, schedulePattern, status, PageRequest.of(page, size, sortOrder));
        return ResponseEntity.ok(ApiResponse.success(pageResponse, "Class schedules retrieved successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('class-schedule:read')")
    public ResponseEntity<ApiResponse<ClassScheduleResponse>> getScheduleById(@PathVariable UUID id) {
        ClassScheduleResponse response = classScheduleService.getScheduleById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Class schedule retrieved successfully"));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('class-schedule:create')")
    public ResponseEntity<ApiResponse<ClassScheduleResponse>> createSchedule(@Valid @RequestBody CreateClassScheduleRequest request) {
        ClassScheduleResponse response = classScheduleService.createSchedule(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Class schedule created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('class-schedule:update')")
    public ResponseEntity<ApiResponse<ClassScheduleResponse>> updateSchedule(
            @PathVariable UUID id, @Valid @RequestBody CreateClassScheduleRequest request) {
        return ResponseEntity.ok(ApiResponse.success(classScheduleService.updateSchedule(id, request),
                "Class schedule updated successfully"));
    }

    @PostMapping("/pattern")
    @PreAuthorize("hasAuthority('class-schedule:create')")
    public ResponseEntity<ApiResponse<SchedulePatternResponse>> createSchedulePattern(@Valid @RequestBody CreateSchedulePatternRequest request) {
        SchedulePatternResponse response = classScheduleService.createSchedulePattern(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Schedule pattern created and sessions/attendance generated successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('class-schedule:delete')")
    public ResponseEntity<ApiResponse<Void>> deleteSchedule(@PathVariable UUID id) {
        classScheduleService.deactivateSchedule(id);
        return ResponseEntity.ok(ApiResponse.success("Class schedule deactivated successfully"));
    }
}
