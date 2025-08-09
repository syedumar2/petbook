package com.petbook.petbook_backend.dto.request;


import com.petbook.petbook_backend.models.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import lombok.AllArgsConstructor;

import lombok.NoArgsConstructor;

import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddPetRequest {
    @NotBlank(message = "Name is required")
    private String name;
    @NotBlank(message = "Pet Type is required")
    private String type;
    private String breed;
    private String description;
    @NotBlank(message = "Pet location is required")
    private String location;

    private List<String> imageUrls;// Updated from single String to List
    private User owner;


}
