package com.petbook.petbook_backend.dto.response;

import com.petbook.petbook_backend.models.Pet;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class PetInfoResponse {


    private String name;
    private String type;
    private String breed;
    private String location;
    private String imageUrl;
    private boolean adopted = false;
    private String owner;

    public PetInfoResponse(Pet post) {
        this.name = post.getName();
        this.type = post.getType();
        this.breed = post.getBreed();
        this.location = post.getLocation();
        this.imageUrl = post.getImageUrl();
        this.adopted = post.isAdopted();
        this.owner = post.getOwner().getEmail();
    }



    public static PetInfoResponse fromEntity(Pet pet){
        return new PetInfoResponse(
                pet.getName(),
                pet.getType(),
                pet.getBreed(),
                pet.getLocation(),
                pet.getImageUrl(),
                pet.isAdopted(),
                pet.getOwner().getEmail()
        );
    }




}
