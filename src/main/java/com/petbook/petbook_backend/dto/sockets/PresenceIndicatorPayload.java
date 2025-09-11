package com.petbook.petbook_backend.dto.sockets;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PresenceIndicatorPayload {
    Long userId;
    boolean online;
}
