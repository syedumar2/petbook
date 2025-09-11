package com.petbook.petbook_backend.service.events;

import com.petbook.petbook_backend.models.NotificationType;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class NotificationEvent {
    private final String recipientEmail;
    private final String message;
    private final NotificationType type;
    private final Long recipientUserId;

}
