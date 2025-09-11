package com.petbook.petbook_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageResponse {
    private Long id;
    private Long senderId;
    private Long receiverId;
    private String receiverName;
    private String senderName;
    private String content;
    private Boolean read;
    private LocalDateTime sentAt;
}