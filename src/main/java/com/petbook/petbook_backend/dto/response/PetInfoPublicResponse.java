package com.petbook.petbook_backend.dto.response;

import com.petbook.petbook_backend.models.Pet;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class PetInfoPublicResponse {


    private String name;
    private String type;
    private String breed;
    private String location;
    private String imageUrl;
    private boolean adopted = false;
    private String owner;

    public PetInfoPublicResponse(Pet post) {
        this.name = post.getName();
        this.type = post.getType();
        this.breed = post.getBreed();
        this.location = post.getLocation();
        this.imageUrl = post.getImageUrl();
        this.adopted = post.isAdopted();
        this.owner = post.getOwner().getEmail();
    }



    public static PetInfoPublicResponse fromEntity(Pet pet){
        return new PetInfoPublicResponse(
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
