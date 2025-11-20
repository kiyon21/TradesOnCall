package com.tradeswift.backend.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Generic API response wrapper")
public class ApiResponse<T> {
    @Schema(description = "Indicates if the request was successful", example = "true")
    private Boolean success;
    
    @Schema(description = "Response message", example = "Operation completed successfully")
    private String message;
    
    @Schema(description = "Response data payload")
    private T data;

    @Builder.Default
    @Schema(description = "Response timestamp", example = "2024-01-15T10:30:00")
    private LocalDateTime timeStamp = LocalDateTime.now();

    // static helper
    public static<T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timeStamp(LocalDateTime.now())
                .build();
    }

    public static<T> ApiResponse<List<T>> success(String message, List<T> list) {
        return ApiResponse.<List<T>>builder()
                .success(true)
                .message(message)
                .data(list)
                .timeStamp(LocalDateTime.now())
                .build();
    }

    public static<T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .timeStamp(LocalDateTime.now())
                .build();
    }
}
