package com.petbook.petbook_backend.service;


import com.petbook.petbook_backend.dto.request.AddPetRequest;
import com.petbook.petbook_backend.dto.request.UpdatePetRequest;
import com.petbook.petbook_backend.dto.response.PageResponse;
import com.petbook.petbook_backend.dto.response.PetInfoPrivateResponse;
import com.petbook.petbook_backend.dto.response.PetInfoPublicResponse;
import com.petbook.petbook_backend.models.Pet;
import com.petbook.petbook_backend.models.User;
import com.petbook.petbook_backend.repository.PetRepository;
import com.petbook.petbook_backend.repository.UserRepository;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PetService {

    private final UserRepository userRepository;
    private final PetRepository petRepository;

    public PetInfoPrivateResponse addPetPost(AddPetRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(username).orElseThrow(() -> new RuntimeException("User not found"));
        request.setOwner(user);
        String email = user.getEmail();

        Pet pet = new Pet();
        pet.setName(request.getName());
        pet.setType(request.getType());
        pet.setBreed(request.getBreed());
        pet.setLocation(request.getLocation());
        pet.setImageUrl(request.getImageUrl());
        pet.setOwner(request.getOwner());

        Pet savedPet = petRepository.save(pet);


        return PetInfoPrivateResponse.builder()
                .id(pet.getId())
                .name(pet.getName())
                .type(pet.getType())
                .breed(pet.getBreed())
                .location(pet.getLocation())
                .imageUrl(pet.getImageUrl())
                .adopted(pet.isAdopted())
                .owner(email)
                .build();


    }

    public List<PetInfoPrivateResponse> getUserPets() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(username).orElseThrow(() -> new RuntimeException("User not found"));
        String email = user.getEmail();
        List<PetInfoPrivateResponse> list = new ArrayList<>();
        petRepository.findByOwnerId(user.getId()).forEach(p -> list.add(PetInfoPrivateResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .type(p.getType())
                .breed(p.getBreed())
                .location(p.getLocation())
                .imageUrl(p.getImageUrl())
                .adopted(p.isAdopted())
                .owner(email)
                .build()));
        return list;
    }

    @Transactional
    public List<PetInfoPublicResponse> getAllPets() {
        List<PetInfoPublicResponse> list = new ArrayList<>();
        petRepository.findAll().forEach(p -> list.add(PetInfoPublicResponse.builder()
                .name(p.getName())
                .type(p.getType())
                .breed(p.getBreed())
                .location(p.getLocation())
                .imageUrl(p.getImageUrl())
                .adopted(p.isAdopted())
                .owner(p.getOwner().getEmail())
                .build()));
        return list;

    }

    @Transactional
    public List<PetInfoPublicResponse> getPetsWithSorting(String field) {
        List<PetInfoPublicResponse> list = new ArrayList<>();
        petRepository.findAll(Sort.by(Sort.Direction.ASC, field)).forEach(p -> list.add(PetInfoPublicResponse.builder()
                .name(p.getName())
                .type(p.getType())
                .breed(p.getBreed())
                .location(p.getLocation())
                .imageUrl(p.getImageUrl())
                .adopted(p.isAdopted())
                .owner(p.getOwner().getEmail())
                .build()));
        return list;


    }

    @Transactional
    public PageResponse<PetInfoPublicResponse> getPetsWithPagination(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PetInfoPublicResponse> petsPage = petRepository.findAll(pageable)
                .map(PetInfoPublicResponse::fromEntity);
        return new PageResponse<>(petsPage);

    }

    @Transactional
    public PetInfoPrivateResponse updatePetPost(UpdatePetRequest request, long petId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        String email = user.getEmail();

        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new RuntimeException("Listing not found"));

        if (!pet.getOwner().getEmail().equals(user.getEmail())) {
            throw new RuntimeException("Pet Listing is not owned by User");
        }


        if (request.getName() != null) pet.setName(request.getName());
        if (request.getType() != null) pet.setType(request.getType());
        if (request.getBreed() != null) pet.setBreed(request.getBreed());
        if (request.getLocation() != null) pet.setLocation(request.getLocation());
        if (request.getImageUrl() != null) pet.setImageUrl(request.getImageUrl());
        if(request.isAdopted()) pet.setAdopted(true);



        Pet savedPet = petRepository.save(pet);


        return PetInfoPrivateResponse.builder()
                .id(pet.getId())
                .name(pet.getName())
                .type(pet.getType())
                .breed(pet.getBreed())
                .location(pet.getLocation())
                .imageUrl(pet.getImageUrl())
                .adopted(pet.isAdopted())
                .owner(email)
                .build();

    }

    @Transactional
    public PetInfoPrivateResponse deletePetPost(@NotNull long petId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        String email = user.getEmail();

        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new RuntimeException("Listing not found"));

        if (!pet.getOwner().getEmail().equals(user.getEmail())) {
            throw new RuntimeException("Pet Listing is not owned by User");
        }
        petRepository.delete(pet);
        return PetInfoPrivateResponse.builder()
                .id(pet.getId())
                .name(pet.getName())
                .type(pet.getType())
                .breed(pet.getBreed())
                .location(pet.getLocation())
                .imageUrl(pet.getImageUrl())
                .adopted(pet.isAdopted())
                .owner(email)
                .build();


    }


}
