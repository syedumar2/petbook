package com.petbook.petbook_backend.dto.response;



import com.petbook.petbook_backend.models.ImageUrl;
import com.petbook.petbook_backend.models.Pet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class PetInfoPublicListingDTO{

    private Long id;
    private String name;
    private String type;
    private String breed;
    private String location;
    private String gender;
    private String imageUrl;
    private boolean adopted = false;
    private String owner;
    private String description;
    private Long ownerId;
    private LocalDateTime createdAt;


    public PetInfoPublicListingDTO(
            Long id,
            String name,
            String type,
            String breed,
            String location,
            String gender,
            String imageUrl,
            boolean adopted,
            String owner,
            String description,
            Long ownerId,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.breed = breed;
        this.location = location;
        this.gender = gender;
        this.imageUrl = imageUrl;
        this.adopted = adopted;
        this.owner = owner;
        this.description = description;
        this.ownerId = ownerId;
        this.createdAt = createdAt;
    }



    public static PetInfoPublicListingDTO fromEntity(Pet pet) {
        return new PetInfoPublicListingDTO(
                pet.getId(),
                pet.getName(),
                pet.getType(),
                pet.getBreed(),
                pet.getLocation(),
                String.valueOf(pet.getGender()),
                pet.getImages().get(0).getUrl(),
                pet.isAdopted(),
                pet.getOwner().getEmail(),
                pet.getDescription(),
                pet.getOwner().getId(),
                pet.getCreatedAt()
        );
    }


}
