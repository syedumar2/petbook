package com.petbook.petbook_backend.service;


import com.petbook.petbook_backend.dto.request.AddPetRequest;
import com.petbook.petbook_backend.dto.response.PageResponse;
import com.petbook.petbook_backend.dto.response.PetInfoResponse;
import com.petbook.petbook_backend.models.Pet;
import com.petbook.petbook_backend.models.User;
import com.petbook.petbook_backend.repository.PetRepository;
import com.petbook.petbook_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedModel;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.BooleanUtils.forEach;

@Service
@RequiredArgsConstructor
public class PetService {

    private final UserRepository userRepository;
    private final PetRepository petRepository;

    public PetInfoResponse addPetPost(AddPetRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(username).orElseThrow(() -> new RuntimeException("User not found"));
        request.setOwner(user);

        Pet pet = new Pet();
        pet.setName(request.getName());
        pet.setType(request.getType());
        pet.setBreed(request.getBreed());
        pet.setLocation(request.getLocation());
        pet.setImageUrl(request.getImageUrl());
        pet.setOwner(request.getOwner());

        Pet savedPet = petRepository.save(pet);

        PetInfoResponse response = new PetInfoResponse();
        response.setName(savedPet.getName());
        response.setType(savedPet.getType());
        response.setType(savedPet.getType());
        response.setBreed(savedPet.getBreed());
        response.setLocation(savedPet.getLocation());
        response.setImageUrl(savedPet.getImageUrl());
        response.setOwner(savedPet.getOwner().getEmail());

        return response;


    }

    public List<PetInfoResponse> getUserPets() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(username).orElseThrow(() -> new RuntimeException("User not found"));
        String email = user.getEmail();
        List<PetInfoResponse> list = new ArrayList<>();
        petRepository.findByOwnerId(user.getId()).forEach(p -> list.add(PetInfoResponse.builder()
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
    public List<PetInfoResponse> getAllPets() {
        List<PetInfoResponse> list = new ArrayList<>();
        petRepository.findAll().forEach(p -> list.add(PetInfoResponse.builder()
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
    public List<PetInfoResponse> getPetsWithSorting(String field) {
        List<PetInfoResponse> list = new ArrayList<>();
        petRepository.findAll(Sort.by(Sort.Direction.ASC, field)).forEach(p -> list.add(PetInfoResponse.builder()
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
    public PageResponse<PetInfoResponse> getPetsWithPagination(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PetInfoResponse> petsPage = petRepository.findAll(pageable)
                .map(PetInfoResponse::fromEntity);
        return new PageResponse<>(petsPage);

    }
    //.map() method on Page<T> handles this transformation + pagination structure automatically
}
