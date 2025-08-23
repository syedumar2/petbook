package com.petbook.petbook_backend.dto.response;

import com.petbook.petbook_backend.models.ImageUrl;
import com.petbook.petbook_backend.models.Pet;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class PetInfoPublicResponse {

        private Long id;
        private String name;
        private String type;
        private String breed;
        private String location;
        private List<String> imageUrls;
        private boolean adopted = false;
        private String owner;
        private String description;

    public PetInfoPublicResponse(Pet post) {
        this.name = post.getName();
        this.type = post.getType();
        this.breed = post.getBreed();
        this.location = post.getLocation();
        this.imageUrls = post.getImages().stream().map(ImageUrl::getUrl).toList();
        this.adopted = post.isAdopted();
        this.owner = post.getOwner().getEmail();
        this.description = post.getDescription();
    }



    public static PetInfoPublicResponse fromEntity(Pet pet){
        return new PetInfoPublicResponse(
                pet.getId(),
                pet.getName(),
                pet.getType(),
                pet.getBreed(),
                pet.getLocation(),
                pet.getImages().stream().map(ImageUrl::getUrl).toList(),
                pet.isAdopted(),
                pet.getOwner().getEmail(),
                pet.getDescription()
        );
    }




}
