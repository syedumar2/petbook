package com.petbook.petbook_backend.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class PetActionRequest {
    List<Long> ids;
}
