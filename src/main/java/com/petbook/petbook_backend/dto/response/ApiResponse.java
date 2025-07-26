package com.petbook.petbook_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;


@Getter
public class ApiResponse<T> {
    private String status;
    private String message;
    private LocalDateTime timestamp;
    private T data;
    private int recordCount;

    public ApiResponse(String status, String message, T data, int recordCount) {
        this.status = status;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
        this.recordCount = recordCount;
    }



    public static <T> ApiResponse<T> success(String message,T data){
        return new ApiResponse<>("success",message,data,0);
    }
    public static <T> ApiResponse<T> failure(String message){
        return new ApiResponse<>("error", message, null,0);
    }
    public static <T> ApiResponse<T> successWithCount(int count,String message,T data){
        return new ApiResponse<>("success",message,data, count);
    }
}
