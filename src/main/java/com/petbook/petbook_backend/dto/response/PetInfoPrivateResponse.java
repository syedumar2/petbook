package com.petbook.petbook_backend.dto.response;


import com.petbook.petbook_backend.models.Pet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class PetInfoPrivateResponse {
    private long id;
    private String name;
    private String type;
    private String breed;
    private String location;
    private String gender;
    private List<Map<String, String>> imageUrls;
    private boolean adopted = false;
    private String owner;
    private String description;
    private Boolean approved;
    private LocalDateTime approvedAt;
    private LocalDateTime rejectedAt;
    private LocalDateTime createdAt;


    public static PetInfoPrivateResponse fromEntity(Pet pet) {
        return new PetInfoPrivateResponse(
                pet.getId(),
                pet.getName(),
                pet.getType(),
                pet.getBreed(),
                pet.getLocation(),
                pet.getGender().toString(),
                pet.getImages().stream().map(img -> {
                    Map<String, String> map = new HashMap<>();
                    map.put(img.getUrl(), img.getPublicId());
                    return map;
                }).collect(Collectors.toList()),
                pet.isAdopted(),
                pet.getOwner().getEmail(),
                pet.getDescription(),
                pet.isApproved(),
                pet.getApprovedAt(),
                pet.getRejectedAt(),
                pet.getCreatedAt()
        );
    }
}

