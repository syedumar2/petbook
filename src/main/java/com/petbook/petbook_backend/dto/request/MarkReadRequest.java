package com.petbook.petbook_backend.dto.request;


import lombok.Getter;

@Getter
public class MarkReadRequest {
    private Long conversationId;
    private Long userId;

}
