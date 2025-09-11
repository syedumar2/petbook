package com.petbook.petbook_backend.dto.request;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FindPetByExampleRequest {
    private String name;
    private String type;
    private String breed;
    private String location;
    private String gender;
    private Boolean adopted;
    private String ownerEmail;
}
