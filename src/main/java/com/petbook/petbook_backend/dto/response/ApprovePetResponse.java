package com.petbook.petbook_backend.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApprovePetResponse {
    private Long petId;
    private boolean approved;
    private String message;
}
