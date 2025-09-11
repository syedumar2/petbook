package com.petbook.petbook_backend.dto.request;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UserActionsRequest {
    List<Long> ids;
}
