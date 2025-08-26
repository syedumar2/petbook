package com.petbook.petbook_backend.dto.request;


import com.petbook.petbook_backend.models.User;
import lombok.Data;

import lombok.AllArgsConstructor;

import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdatePetRequest {

    private String name;
    private String type;
    private String breed;
    private String location;
    private List<Map<String,String>> imageUrls = new ArrayList<>();
    private Boolean adopted;
    private String description;



}
