package com.petbook.petbook_backend.dto.request;


import com.petbook.petbook_backend.models.User;
import lombok.Data;

import lombok.AllArgsConstructor;

import lombok.NoArgsConstructor;

import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdatePetRequest {

    private String name;
    private String type;
    private String breed;
    private String location;
    private List<String> imageUrls;
    private boolean adopted;



}
