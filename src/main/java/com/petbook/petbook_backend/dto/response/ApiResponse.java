package com.petbook.petbook_backend.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
public class ApiResponse<T> {
    private final boolean success;
    private final String message;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime timestamp;

    private final T data;
    private final int recordCount;

    private ApiResponse(boolean success, String message, T data, int recordCount) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.recordCount = recordCount;
        this.timestamp = LocalDateTime.now();
    }

    // ✅ Factory methods
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data, 0);
    }

    public static <T> ApiResponse<T> successWithCount(int count, String message, T data) {
        return new ApiResponse<>(true, message, data, count);
    }

    public static <T> ApiResponse<T> failure(String message) {
        return new ApiResponse<>(false, message, null, 0);
    }

    public static ApiResponse<Map<String, String>> failureInValidation(String message, Map<String, String> data) {
        return new ApiResponse<>(false, message, data, data != null ? data.size() : 0);
    }
}
