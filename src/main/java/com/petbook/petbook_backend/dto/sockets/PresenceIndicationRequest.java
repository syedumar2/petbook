package com.petbook.petbook_backend.dto.sockets;

import lombok.Data;

@Data
public class PresenceIndicationRequest {
    Long userId;
    boolean online;
    Long conversationId;
}
