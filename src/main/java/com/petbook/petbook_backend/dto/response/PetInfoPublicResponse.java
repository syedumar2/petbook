package com.petbook.petbook_backend.dto.response;

import com.petbook.petbook_backend.models.ImageUrl;
import com.petbook.petbook_backend.models.Pet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    private String gender;
    private List<String> imageUrls;
    private boolean adopted = false;
    private String owner;
    private String description;
    private Long ownerId;

    public PetInfoPublicResponse(Pet post) {
        this.name = post.getName();
        this.type = post.getType();
        this.breed = post.getBreed();
        this.location = post.getLocation();
        this.gender = String.valueOf(post.getGender());
        this.imageUrls = post.getImages().stream().map(ImageUrl::getUrl).toList();
        this.adopted = post.isAdopted();
        this.owner = post.getOwner().getEmail();
        this.description = post.getDescription();
    }


    public static PetInfoPublicResponse fromEntity(Pet pet) {
        return new PetInfoPublicResponse(
                pet.getId(),
                pet.getName(),
                pet.getType(),
                pet.getBreed(),
                pet.getLocation(),
                String.valueOf(pet.getGender()),
                pet.getImages().stream().map(ImageUrl::getUrl).toList(),
                pet.isAdopted(),
                pet.getOwner().getEmail(),
                pet.getDescription(),
                pet.getOwner().getId()
        );
    }


}
