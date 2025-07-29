package com.petbook.petbook_backend.dto.response;


import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PetInfoPrivateResponse {
    private long id;
    private String name;
    private String type;
    private String breed;
    private String location;
    private String imageUrl;
    private boolean adopted = false;
    private String owner;

}
