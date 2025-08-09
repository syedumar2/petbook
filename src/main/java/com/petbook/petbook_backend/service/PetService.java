package com.petbook.petbook_backend.service;


import com.petbook.petbook_backend.dto.request.AddPetRequest;
import com.petbook.petbook_backend.dto.request.FindPetByExampleRequest;
import com.petbook.petbook_backend.dto.request.UpdatePetRequest;
import com.petbook.petbook_backend.dto.response.PageResponse;
import com.petbook.petbook_backend.dto.response.PetInfoPrivateResponse;
import com.petbook.petbook_backend.dto.response.PetInfoPublicResponse;
import com.petbook.petbook_backend.exceptions.PetListingNotFoundException;
import com.petbook.petbook_backend.exceptions.UnauthorizedUserException;
import com.petbook.petbook_backend.exceptions.UserNotFoundException;
import com.petbook.petbook_backend.models.Pet;
import com.petbook.petbook_backend.models.User;
import com.petbook.petbook_backend.repository.PetRepository;
import com.petbook.petbook_backend.repository.UserRepository;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
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
        User user = userRepository.findByEmail(username).orElseThrow(() -> new UserNotFoundException("User not found"));
        request.setOwner(user);
        String email = user.getEmail();

        Pet savedPet = petRepository.save(Pet.builder()
                .name(request.getName())
                .type(request.getType())
                .breed(request.getBreed())
                .location(request.getLocation())
                .imageUrls(request.getImageUrls())
                .description(request.getDescription())
                .owner(request.getOwner())
                .build());


        return PetInfoPrivateResponse.builder()
                .id(savedPet.getId())
                .name(savedPet.getName())
                .type(savedPet.getType())
                .breed(savedPet.getBreed())
                .location(savedPet.getLocation())
                .imageUrls(savedPet.getImageUrls())
                .adopted(savedPet.isAdopted())
                .owner(email)
                .description(savedPet.getDescription())
                .build();


    }

    public List<PetInfoPrivateResponse> getUserPets() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(username).orElseThrow(() -> new UserNotFoundException("User not found"));
        String email = user.getEmail();
        List<PetInfoPrivateResponse> list = new ArrayList<>();
        petRepository.findByOwnerId(user.getId()).forEach(p -> list.add(PetInfoPrivateResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .type(p.getType())
                .breed(p.getBreed())
                .location(p.getLocation())
                .imageUrls(p.getImageUrls())
                .adopted(p.isAdopted())
                .description(p.getDescription())
                .owner(email)
                .build()));
        return list;
    }

    @Transactional
    public List<PetInfoPublicResponse> getAllPets() {
        List<PetInfoPublicResponse> list = new ArrayList<>();
        petRepository
                .findByApproved(true)
                .forEach(p -> {

                    List<String> urls = new ArrayList<>(p.getImageUrls());

                    list.add(PetInfoPublicResponse.builder()
                            .name(p.getName())
                            .type(p.getType())
                            .breed(p.getBreed())
                            .location(p.getLocation())
                            .imageUrls(urls)
                            .adopted(p.isAdopted())
                            .owner(p.getOwner().getEmail())
                            .description(p.getDescription())

                            .build());
                });
        return list;

    }

    @Transactional
    public List<PetInfoPublicResponse> getPetsWithSorting(String field) {
        List<String> allowedFields = List.of("name", "type", "breed", "location", "adopted");
        if (!allowedFields.contains(field)) {
            throw new IllegalArgumentException("Invalid sort field: " + field);
        }

        List<PetInfoPublicResponse> list = new ArrayList<>();
        petRepository.findAll((root,query,cb)->cb.isTrue(root.get("approved")),
                Sort.by(Sort.Direction.ASC, field)).forEach(p -> list.add(PetInfoPublicResponse.builder()
                .name(p.getName())
                .type(p.getType())
                .breed(p.getBreed())
                .location(p.getLocation())
                .imageUrls(p.getImageUrls())
                .adopted(p.isAdopted())
                .owner(p.getOwner().getEmail())
                .description(p.getDescription())
                .build()));
        return list;


    }

    @Transactional
    public PageResponse<PetInfoPublicResponse> getPetsWithPaginationAndSorting(int page, int size, String sortField, String sortDirection) {
        List<String> allowedFields = List.of("name", "type", "breed", "location", "adopted");
        if (!allowedFields.contains(sortField)) {
            throw new IllegalArgumentException("Invalid sort field: " + sortField);
        }

        Sort.Direction direction;
        try {
            direction = Sort.Direction.fromString(sortDirection);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid sort direction: " + sortDirection + ". Use 'asc' or 'desc'.");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

        Page<PetInfoPublicResponse> petsPage = petRepository.findAll(
                (root, query, cb) -> cb.isTrue(root.get("approved")),
                pageable
        ).map(PetInfoPublicResponse::fromEntity);

        return new PageResponse<>(petsPage);
    }


    @Transactional
    public PageResponse<PetInfoPublicResponse> getPetsWithPagination(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PetInfoPublicResponse> petsPage = petRepository.findAll((root,query,cb)->cb.isTrue(root.get("approved"))
                ,pageable)
                .map(PetInfoPublicResponse::fromEntity);
        return new PageResponse<>(petsPage);

    }

    @Transactional
    public PetInfoPrivateResponse updatePetPost(UpdatePetRequest request, long petId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        String email = user.getEmail();

        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new PetListingNotFoundException("Listing not found:" + petId));

        if (!pet.getOwner().getEmail().equals(user.getEmail())) {
            throw new UnauthorizedUserException("Pet Listing is not owned by User");
        }


        applyUpdates(pet, request);
        pet = petRepository.save(pet);


        return PetInfoPrivateResponse.builder()
                .id(pet.getId())
                .name(pet.getName())
                .type(pet.getType())
                .breed(pet.getBreed())
                .location(pet.getLocation())
                .imageUrls(pet.getImageUrls())
                .adopted(pet.isAdopted())
                .owner(email)
                .description(pet.getDescription())
                .build();

    }

    @Transactional
    public PetInfoPrivateResponse deletePetPost(@NotNull long petId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        String email = user.getEmail();

        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new PetListingNotFoundException("Listing not found"));

        if (!pet.getOwner().getEmail().equals(user.getEmail())) {
            throw new UnauthorizedUserException("Pet Listing is not owned by User");
        }
        petRepository.delete(pet);
        return PetInfoPrivateResponse.builder()
                .id(pet.getId())
                .name(pet.getName())
                .type(pet.getType())
                .breed(pet.getBreed())
                .location(pet.getLocation())
                .imageUrls(pet.getImageUrls())
                .adopted(pet.isAdopted())
                .owner(email)
                .build();


    }

    //find all pets matching exact criteria
    @Transactional
    public List<PetInfoPublicResponse> findPetsByExample(FindPetByExampleRequest request) {
        Pet probe = Pet.builder()
                .name(request.getName())
                .type(request.getType())
                .breed(request.getBreed())
                .location(request.getLocation())
                .adopted(request.getAdopted() != null ? request.getAdopted() : false) // you can also skip this if null
                .owner(request.getOwnerEmail() != null ?
                        User.builder().email(request.getOwnerEmail()).build() : null)
                .build();
        ExampleMatcher exampleMatcher = ExampleMatcher.matchingAll()
                .withIgnoreNullValues()
                .withIgnorePaths("id", "description", "imageUrls")
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
                .withIgnoreCase();
        List<PetInfoPublicResponse> list = new ArrayList<>();
        Example<Pet> example = Example.of(probe, exampleMatcher);
        petRepository.findAll(example).forEach(p ->
                list.add(PetInfoPublicResponse.fromEntity(p)));
        return list;

    }


    public List<PetInfoPublicResponse> searchPets(String name, String type, String breed, String location) {
        Pet probe = Pet.builder()
                .name(name)
                .type(type)
                .breed(breed)
                .location(location)
                .build();

        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnoreCase()
                .withIgnoreNullValues()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING); // if you want partial matches

        Example<Pet> example = Example.of(probe, matcher);
        List<PetInfoPublicResponse> list = new ArrayList<>();
        petRepository.findAll(example).forEach(p ->
                list.add(PetInfoPublicResponse.fromEntity(p)));
        return list;

    }

    private void applyUpdates(Pet pet, UpdatePetRequest request) {
        if (request.getName() != null) pet.setName(request.getName());
        if (request.getType() != null) pet.setType(request.getType());
        if (request.getBreed() != null) pet.setBreed(request.getBreed());
        if (request.getLocation() != null) pet.setLocation(request.getLocation());
        if (request.getImageUrls() != null) pet.setImageUrls(request.getImageUrls());
        if (request.getDescription() != null) pet.setDescription(request.getDescription());
        if (request.getAdopted() != null) pet.setAdopted(request.getAdopted());
    }
}
