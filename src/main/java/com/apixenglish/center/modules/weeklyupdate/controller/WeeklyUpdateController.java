package com.apixenglish.center.modules.weeklyupdate.controller;
import com.apixenglish.center.common.response.ApiResponse;
import com.apixenglish.center.modules.weeklyupdate.dto.WeeklyUpdateDtos.*;
import com.apixenglish.center.modules.weeklyupdate.service.WeeklyUpdateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;
@RestController @RequestMapping("/api/v1/weekly-updates") @RequiredArgsConstructor
public class WeeklyUpdateController {
 private final WeeklyUpdateService service;
 @GetMapping @PreAuthorize("hasAuthority('weekly_update:read')") public ApiResponse<Page<Response>> list(@RequestParam UUID classId,@RequestParam(defaultValue="0")int page,@RequestParam(defaultValue="20")int size){return ApiResponse.success(service.list(classId,PageRequest.of(page,size)));}
 @GetMapping("/{id}") @PreAuthorize("hasAuthority('weekly_update:read')") public ApiResponse<Response> get(@PathVariable UUID id){return ApiResponse.success(service.get(id));}
 @PostMapping @PreAuthorize("hasAuthority('weekly_update:create')") public ApiResponse<Response> create(@Valid @RequestBody Upsert request){return ApiResponse.success(service.create(request));}
 @PutMapping("/{id}") @PreAuthorize("hasAuthority('weekly_update:update')") public ApiResponse<Response> update(@PathVariable UUID id,@Valid @RequestBody Upsert request){return ApiResponse.success(service.update(id,request));}
 @PostMapping("/{id}/submit") @PreAuthorize("hasAuthority('weekly_update:submit')") public ApiResponse<Response> submit(@PathVariable UUID id){return ApiResponse.success(service.submit(id));}
 @PostMapping("/{id}/approve") @PreAuthorize("hasAuthority('weekly_update:approve')") public ApiResponse<Response> approve(@PathVariable UUID id){return ApiResponse.success(service.approve(id));}
 @PostMapping("/{id}/reject") @PreAuthorize("hasAuthority('weekly_update:reject')") public ApiResponse<Response> reject(@PathVariable UUID id,@Valid @RequestBody Reject request){return ApiResponse.success(service.reject(id,request.reason()));}
 @PostMapping({"/{id}/deliver","/{id}/publish"}) @PreAuthorize("hasAuthority('weekly_update:deliver')") public ApiResponse<Response> deliver(@PathVariable UUID id){return ApiResponse.success(service.deliver(id));}
}
