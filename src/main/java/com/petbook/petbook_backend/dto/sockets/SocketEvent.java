package com.petbook.petbook_backend.dto.sockets;

import com.petbook.petbook_backend.models.EventType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;


@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class SocketEvent<T> {
    private EventType type;
    private T payload;
}
