package com.petbook.petbook_backend.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class NotificationsDeleteRequest {
    List<Long> ids;
}
