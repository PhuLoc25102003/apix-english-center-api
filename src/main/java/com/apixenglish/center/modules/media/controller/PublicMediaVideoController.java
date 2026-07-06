package com.apixenglish.center.modules.media.controller;
import com.apixenglish.center.common.response.ApiResponse; import com.apixenglish.center.modules.media.dto.VideoDtos.PublicVideo; import com.apixenglish.center.modules.media.service.MediaVideoService; import lombok.RequiredArgsConstructor; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/public/media/videos") @RequiredArgsConstructor
public class PublicMediaVideoController { private final MediaVideoService service; @GetMapping("/{shareToken}") public ApiResponse<PublicVideo> resolve(@PathVariable String shareToken){return ApiResponse.success(service.resolve(shareToken));} }
