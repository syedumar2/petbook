package com.petbook.petbook_backend.service;

import com.petbook.petbook_backend.dto.response.PetInfoPrivateResponse;
import com.petbook.petbook_backend.dto.response.UserDetailsResponse;
import com.petbook.petbook_backend.models.Pet;
import com.petbook.petbook_backend.models.User;
import com.petbook.petbook_backend.repository.PetRepository;
import com.petbook.petbook_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final PetRepository petRepository;

    public List<PetInfoPrivateResponse> getApprovedPets() {
        return petRepository.findByApproved(true)
                .stream()
                .map(this::mapToPetInfoPrivateResponse)
                .toList();
    }

    public List<PetInfoPrivateResponse> getUnapprovedPets() {
        return petRepository.findByApproved(false)
                .stream()
                .map(this::mapToPetInfoPrivateResponse)
                .toList();
    }

    public PetInfoPrivateResponse approvePet(Long petId) {
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new RuntimeException("Pet not found"));
        pet.setApproved(true);
        pet.setApprovedAt(LocalDateTime.now());
        pet.setRejectedAt(null);
        petRepository.save(pet);
        return mapToPetInfoPrivateResponse(pet);
    }

    public PetInfoPrivateResponse rejectPet(Long petId) {
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new RuntimeException("Pet not found"));

        pet.setRejectedAt(LocalDateTime.now());
        return mapToPetInfoPrivateResponse(pet);
    }

    public List<UserDetailsResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToUserInfoResponse)
                .toList();
    }

    private PetInfoPrivateResponse mapToPetInfoPrivateResponse(Pet pet) {
        return PetInfoPrivateResponse.builder()
                .id(pet.getId())
                .name(pet.getName())
                .type(pet.getType())
                .breed(pet.getBreed())
                .location(pet.getLocation())
                .description(pet.getDescription())
                .adopted(pet.isAdopted())
                .approved(pet.isApproved())
                .owner(pet.getOwner() != null ? pet.getOwner().getEmail() : null)
                .build();
    }

    private UserDetailsResponse mapToUserInfoResponse(User user) {
        return UserDetailsResponse.builder()
                .id(user.getId())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .email(user.getEmail())
                .location(user.getLocation())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .profileImageUrl(user.getProfileImageUrl())
                .build();
    }
}
