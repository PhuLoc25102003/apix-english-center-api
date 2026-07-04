package com.apixenglish.center.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private PageMeta meta;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PageMeta {
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
    }

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message("Success")
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    public static ApiResponse<Void> success() {
        return ApiResponse.<Void>builder()
                .success(true)
                .message("Success")
                .build();
    }

    public static ApiResponse<Void> success(String message) {
        return ApiResponse.<Void>builder()
                .success(true)
                .message(message)
                .build();
    }

    public static <T> ApiResponse<List<T>> success(PageResponse<T> pageResponse) {
        return ApiResponse.<List<T>>builder()
                .success(true)
                .message("Success")
                .data(pageResponse.getContent())
                .meta(PageMeta.builder()
                        .page(pageResponse.getPage())
                        .size(pageResponse.getSize())
                        .totalElements(pageResponse.getTotalElements())
                        .totalPages(pageResponse.getTotalPages())
                        .build())
                .build();
    }

    public static <T> ApiResponse<List<T>> success(PageResponse<T> pageResponse, String message) {
        return ApiResponse.<List<T>>builder()
                .success(true)
                .message(message)
                .data(pageResponse.getContent())
                .meta(PageMeta.builder()
                        .page(pageResponse.getPage())
                        .size(pageResponse.getSize())
                        .totalElements(pageResponse.getTotalElements())
                        .totalPages(pageResponse.getTotalPages())
                        .build())
                .build();
    }
}
