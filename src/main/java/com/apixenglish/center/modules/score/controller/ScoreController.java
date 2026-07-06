package com.apixenglish.center.modules.score.controller;
import com.apixenglish.center.common.response.ApiResponse;import com.apixenglish.center.modules.score.dto.ScoreDtos.*;import com.apixenglish.center.modules.score.service.ScoreService;import jakarta.validation.Valid;import lombok.RequiredArgsConstructor;import org.springframework.data.domain.*;import org.springframework.security.access.prepost.PreAuthorize;import org.springframework.web.bind.annotation.*;import java.util.UUID;
@RestController @RequestMapping("/api/v1/score-items") @RequiredArgsConstructor public class ScoreController{private final ScoreService service;
 @GetMapping @PreAuthorize("hasAuthority('score:read')") public ApiResponse<Page<Response>> list(@RequestParam UUID classId,@RequestParam(defaultValue="0")int page,@RequestParam(defaultValue="20")int size){return ApiResponse.success(service.list(classId,PageRequest.of(page,size)));}
 @GetMapping("/{id}") @PreAuthorize("hasAuthority('score:read')") public ApiResponse<Response> get(@PathVariable UUID id){return ApiResponse.success(service.get(id));}
 @PostMapping @PreAuthorize("hasAuthority('score:create')") public ApiResponse<Response> create(@Valid @RequestBody Upsert r){return ApiResponse.success(service.create(r));}
 @PutMapping("/{id}") @PreAuthorize("hasAuthority('score:update')") public ApiResponse<Response> update(@PathVariable UUID id,@Valid @RequestBody Upsert r){return ApiResponse.success(service.update(id,r));}
 @PutMapping("/{id}/records") @PreAuthorize("hasAuthority('score:update')") public ApiResponse<Response> records(@PathVariable UUID id,@Valid @RequestBody Records r){return ApiResponse.success(service.records(id,r));}
 @PostMapping("/{id}/publish") @PreAuthorize("hasAuthority('score:update')") public ApiResponse<Response> publish(@PathVariable UUID id){return ApiResponse.success(service.publish(id));}
 @PostMapping("/{id}/deliver") @PreAuthorize("hasAuthority('score:deliver')") public ApiResponse<Response> deliver(@PathVariable UUID id){return ApiResponse.success(service.deliver(id));}
}
