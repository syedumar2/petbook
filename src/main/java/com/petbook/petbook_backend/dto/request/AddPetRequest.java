package com.petbook.petbook_backend.dto.request;


import com.petbook.petbook_backend.models.User;
import lombok.Data;

import lombok.AllArgsConstructor;

import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddPetRequest {
    private String name;
    private String type;
    private String breed;
    private String location;
    private String imageUrl;
    private User owner;


}
