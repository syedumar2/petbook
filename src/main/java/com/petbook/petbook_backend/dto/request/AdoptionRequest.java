package com.petbook.petbook_backend.dto.request;


import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdoptionRequest {
    @NotNull(message = "Adopted flag must be provided")
    Boolean adopted;
}
