package com.apixenglish.center.modules.videodelivery.controller;
import com.apixenglish.center.common.response.*; import com.apixenglish.center.modules.media.entity.VideoType;
import com.apixenglish.center.modules.videodelivery.dto.VideoDeliveryDtos.*; import com.apixenglish.center.modules.videodelivery.entity.VideoBatchStatus;
import com.apixenglish.center.modules.videodelivery.service.VideoDeliveryService; import jakarta.validation.Valid; import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*; import org.springframework.data.web.PageableDefault; import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize; import org.springframework.web.bind.annotation.*; import java.time.LocalDate; import java.util.*;
@RestController @RequestMapping("/api/v1/video-delivery-batches") @RequiredArgsConstructor
public class VideoDeliveryBatchController {
 private final VideoDeliveryService service;
 @GetMapping @PreAuthorize("hasAuthority('video-delivery:read')") public ApiResponse<List<BatchItem>> list(@RequestParam(required=false)String search,@RequestParam(required=false)UUID classId,@RequestParam(required=false)VideoType videoType,@RequestParam(required=false)@DateTimeFormat(iso=DateTimeFormat.ISO.DATE)LocalDate targetMonth,@RequestParam(required=false)VideoBatchStatus status,@RequestParam(required=false)@DateTimeFormat(iso=DateTimeFormat.ISO.DATE)LocalDate dueDateFrom,@RequestParam(required=false)@DateTimeFormat(iso=DateTimeFormat.ISO.DATE)LocalDate dueDateTo,@PageableDefault(size=20,sort="createdAt",direction=Sort.Direction.DESC)Pageable pageable){return ApiResponse.success(service.listBatches(search,classId,videoType,targetMonth,status,dueDateFrom,dueDateTo,pageable));}
 @PostMapping @PreAuthorize("hasAuthority('video-delivery:create-batch')") public ApiResponse<BatchItem> create(@Valid @RequestBody CreateBatch request){return ApiResponse.success(service.createBatch(request));}
 @PutMapping("/{id}") @PreAuthorize("hasAuthority('video-delivery:update-batch')") public ApiResponse<BatchItem> update(@PathVariable UUID id,@Valid @RequestBody UpdateBatch request){return ApiResponse.success(service.updateBatch(id,request));}
 @PatchMapping("/{id}/cancel") @PreAuthorize("hasAuthority('video-delivery:cancel-batch')") public ApiResponse<Void> cancel(@PathVariable UUID id){service.cancelBatch(id);return ApiResponse.success("Video delivery batch cancelled");}
}
