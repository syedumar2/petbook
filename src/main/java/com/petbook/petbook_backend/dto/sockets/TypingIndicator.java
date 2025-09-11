package com.petbook.petbook_backend.dto.sockets;

import lombok.Data;

@Data
public class TypingIndicator {
    private Long conversationId;
    private String username;
    private boolean typing;

}
