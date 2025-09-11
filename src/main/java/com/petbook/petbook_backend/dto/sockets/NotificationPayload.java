package com.petbook.petbook_backend.dto.sockets;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationPayload {
    private Long id;
    private String message;
    private String type;
    private boolean read;
    private String createdAt;

}