package com.petbook.petbook_backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ChatMessageRequest {
    private Long conversationId;
    private Long senderId;
    private Long receiverId;
    private String content;
}

