package com.petbook.petbook_backend.dto.response;


import lombok.Data;

import java.time.LocalDateTime;
@Data
public class PetInfoPrivateListingDTO {

    private Long id;
    private String name;
    private String type;
    private String breed;
    private String location;
    private String gender;
    private String imageUrl;
    private boolean adopted;
    private String owner;
    private Long ownerId;
    private String description;
    private Boolean approved;
    private LocalDateTime approvedAt;
    private LocalDateTime rejectedAt;
    private LocalDateTime createdAt;

    // Updated constructor to include approved fields
    public PetInfoPrivateListingDTO(
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
            Boolean approved,
            LocalDateTime approvedAt,
            LocalDateTime rejectedAt,
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
        this.approved = approved;
        this.approvedAt = approvedAt;
        this.rejectedAt = rejectedAt;
        this.createdAt = createdAt;
    }


}
