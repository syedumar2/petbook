package com.petbook.petbook_backend.service;


import com.petbook.petbook_backend.dto.request.AddPetRequest;
import com.petbook.petbook_backend.dto.request.FindPetByExampleRequest;
import com.petbook.petbook_backend.dto.request.UpdatePetRequest;
import com.petbook.petbook_backend.dto.response.PageResponse;
import com.petbook.petbook_backend.dto.response.PetInfoPrivateResponse;
import com.petbook.petbook_backend.dto.response.PetInfoPublicResponse;
import com.petbook.petbook_backend.exceptions.rest.PetListingNotFoundException;
import com.petbook.petbook_backend.exceptions.rest.UnauthorizedUserException;
import com.petbook.petbook_backend.exceptions.rest.UserNotFoundException;
import com.petbook.petbook_backend.models.Gender;
import com.petbook.petbook_backend.models.ImageUrl;
import com.petbook.petbook_backend.models.NotificationType;
import com.petbook.petbook_backend.models.Pet;
import com.petbook.petbook_backend.models.User;
import com.petbook.petbook_backend.repository.PetImageUrlsRepository;
import com.petbook.petbook_backend.repository.PetRepository;
import com.petbook.petbook_backend.repository.UserRepository;
import com.petbook.petbook_backend.service.events.NotificationEvent;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PetService {

    private final UserRepository userRepository;
    private final PetRepository petRepository;
    private final PetImageUrlsRepository petImageUrlsRepository;
    private final CloudinaryService cloudinaryService;
    private final ApplicationEventPublisher applicationEventPublisher;


//TODO: Change general pet listings query api to only read one imageUrl from the db


    //ALL READ SERVICES
    @Transactional(readOnly = true)
    public PetInfoPrivateResponse findUserPet(@NotNull long petId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new PetListingNotFoundException("Listing not found:" + petId));

        if (!pet.getOwner().getEmail().equals(user.getEmail())) {
            throw new UnauthorizedUserException("Pet Listing is not owned by User");
        }
        return PetInfoPrivateResponse.fromEntity(pet);
    }

    @Transactional(readOnly = true)
    public PetInfoPublicResponse getPetById(Long id) {
        Pet pet = petRepository.findByIdAndApproved(id, true).orElseThrow(() ->
                new PetListingNotFoundException("No Pet listing found for given id"));
        String ownerEmail;
        Long ownerId;
        if (pet.getOwner() != null) {
            ownerEmail = pet.getOwner().getEmail();
            ownerId = pet.getOwner().getId();
        } else {
            ownerEmail = null;
            ownerId = null;
        }
        List<ImageUrl> imageUrls = pet.getImages();
        return PetInfoPublicResponse.builder()
                .id(pet.getId())
                .name(pet.getName())
                .type(pet.getType())
                .breed(pet.getBreed())
                .location(pet.getLocation())
                .description(pet.getDescription())
                .gender(pet.getGender().toString())
                .imageUrls(imageUrls.stream().map(ImageUrl::getUrl).collect(Collectors.toList()))
                .adopted(pet.isAdopted())
                .owner(ownerEmail)
                .ownerId(ownerId)
                .build();
    }

    public List<String> autocomplete(String field, String query) {
        return switch (field.toLowerCase()) {
            case "name" -> petRepository.findTop10ByNameIgnoreCaseStartingWith(query)
                    .stream()
                    .map(Pet::getName)
                    .toList();
            case "type" -> petRepository.findDistinctTop10ByTypeIgnoreCaseStartingWith(query)
                    .stream()
                    .map(Pet::getType)
                    .toList();
            case "breed" -> petRepository.findDistinctTop10ByBreedIgnoreCaseStartingWith(query)
                    .stream()
                    .map(Pet::getBreed)
                    .toList();

            case "location" -> petRepository.findDistinctTop10ByLocationIgnoreCaseStartingWith(query)
                    .stream()
                    .map(Pet::getLocation)
                    .toList();

            default -> List.of();
        };
    }

    @Transactional(readOnly = true)
    public PageResponse<PetInfoPrivateResponse> getUserPets(String sortField, String sortDirection, int page, int size) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(username).orElseThrow(() -> new UserNotFoundException("User not found"));


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
        Page<PetInfoPrivateResponse> petsPage = petRepository.findByOwnerId(user.getId(), pageable).map(PetInfoPrivateResponse::fromEntity);
        return new PageResponse<>(petsPage);

    }

    @Transactional(readOnly = true)
    public PageResponse<PetInfoPublicResponse> getPetsWithPaginationAndSorting(int page, int size, String sortField, String sortDirection) {
        List<String> allowedFields = List.of("name", "type", "breed", "location", "adopted", "gender");
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


    @Transactional(readOnly = true)
    public PageResponse<PetInfoPublicResponse> searchPets(String name, String type, String breed, String location, int page, int size, String sortField, String sortDirection) {

        List<String> allowedFields = List.of("name", "type", "breed", "location", "adopted", "gender");
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


        Pet probe = Pet.builder()
                .name(name)
                .type(type)
                .breed(breed)
                .location(location)
                .approved(true)
                .build();

        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnoreCase()
                .withIgnoreNullValues()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING); // if you want partial matches

        Example<Pet> example = Example.of(probe, matcher);

        List<PetInfoPublicResponse> list = new ArrayList<>();
        Page<PetInfoPublicResponse> petsPage = petRepository.findAll(example, pageable
        ).map(PetInfoPublicResponse::fromEntity);

        return new PageResponse<>(petsPage);
    }


    @Transactional(readOnly = true)
    public PageResponse<PetInfoPublicResponse> findPetsByExample(FindPetByExampleRequest request, int page, int size, String sortField, String sortDirection) {
        List<String> allowedFields = List.of("name", "type", "breed", "location", "adopted", "gender");
        if (!allowedFields.contains(sortField)) {
            throw new IllegalArgumentException("Invalid sort field: " + sortField);
        }

        Sort.Direction direction;
        try {
            direction = Sort.Direction.fromString(sortDirection);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid sort direction: " + sortDirection + ". Use 'asc' or 'desc'.");
        }


        Pet probe = Pet.builder()
                .name(request.getName())
                .type(request.getType())
                .breed(request.getBreed())
                .location(request.getLocation())
                .gender(request.getGender() != null ? Gender.valueOf(request.getGender()) : null)
                .adopted(request.getAdopted() != null ? request.getAdopted() : false)
                .owner(request.getOwnerEmail() != null ?
                        User.builder().email(request.getOwnerEmail()).build() : null)
                .approved(true)
                .build();
        ExampleMatcher exampleMatcher = ExampleMatcher.matchingAll()
                .withIgnoreNullValues()
                .withIgnorePaths("id", "description", "imageUrls")
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
                .withIgnoreCase();
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
        List<PetInfoPublicResponse> list = new ArrayList<>();
        Example<Pet> example = Example.of(probe, exampleMatcher);

        Page<PetInfoPublicResponse> petsPage = petRepository.findAll(example, pageable).map(PetInfoPublicResponse::fromEntity);

        return new PageResponse<>(petsPage);
    }


    //ALL WRITE SERVICES

    @Transactional
    public PetInfoPrivateResponse deletePetPost(@NotNull long petId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        Long userId = user.getId();
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new PetListingNotFoundException("Listing not found"));
        List<Map<String, String>> imageUrls = pet.getImages().stream().map(img -> {
            Map<String, String> map = new HashMap<>();
            map.put(img.getUrl(), img.getPublicId());
            return map;
        }).toList();

        if (!pet.getOwner().getEmail().equals(user.getEmail())) {
            throw new UnauthorizedUserException("Pet Listing is not owned by User");
        }
        // remove old images safely
        List<ImageUrl> imageUrl = pet.getImages();
        imageUrl.forEach(img -> cloudinaryService.deleteFile(img.getPublicId()));
        petRepository.delete(pet);

        applicationEventPublisher.publishEvent(
                NotificationEvent.builder()
                        .recipientEmail(username)
                        .recipientUserId(userId)
                        .message("Your Pet listing " + pet.getName() + " was successfully removed and is no longer Live")
                        .type(NotificationType.PET_DELETED).build()
        );

        return PetInfoPrivateResponse.builder()
                .id(pet.getId())
                .name(pet.getName())
                .type(pet.getType())
                .breed(pet.getBreed())
                .location(pet.getLocation())
                .imageUrls(imageUrls)
                .adopted(pet.isAdopted())
                .owner(username)
                .build();


    }


    //find all pets matching exact criteria

    @Transactional
    public PetInfoPrivateResponse addPetPost(AddPetRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(username).orElseThrow(() -> new UserNotFoundException("User not found"));
        Long userId = user.getId();
        request.setOwner(user);
        Gender gender = Gender.valueOf(request.getGender());


        Pet savedPet = petRepository.save(Pet.builder()
                .name(request.getName())
                .type(request.getType())
                .breed(request.getBreed())
                .location(request.getLocation())
                .gender(gender)
                .description(request.getDescription())
                .owner(request.getOwner())
                .approved(true)
                .approvedAt(LocalDateTime.now())
                .build());

        List<ImageUrl> imageUrls = new ArrayList<>();
        for (Map<String, String> map : request.getImageUrls()) {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                imageUrls.add(ImageUrl.builder()
                        .pet(savedPet)
                        .url(entry.getKey())
                        .publicId(entry.getValue()).build());
            }

        }
        petImageUrlsRepository.saveAll(imageUrls);

        applicationEventPublisher.publishEvent(
                NotificationEvent.builder()
                        .recipientEmail(username)
                        .recipientUserId(userId)
                        .message("Your Pet listing " + savedPet.getName() + " is now Live")
                        .type(NotificationType.PET_APPROVED).build()
        );

        return PetInfoPrivateResponse.builder()
                .id(savedPet.getId())
                .name(savedPet.getName())
                .type(savedPet.getType())
                .breed(savedPet.getBreed())
                .location(savedPet.getLocation())
                .imageUrls(imageUrls.stream().map(img -> {
                    Map<String, String> map = new HashMap<>();
                    map.put(img.getUrl(), img.getPublicId());
                    return map;
                }).toList())
                .adopted(savedPet.isAdopted())
                .owner(username)
                .description(savedPet.getDescription())
                .build();


    }

    @Transactional
    public PetInfoPrivateResponse updatePetAdoptionStatus(long petId, Boolean adopted) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        Long userId = user.getId();


        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new PetListingNotFoundException("Listing not found:" + petId));

        if (!pet.getOwner().getEmail().equals(user.getEmail())) {
            throw new UnauthorizedUserException("Pet Listing is not owned by User");
        }
        pet.setAdopted(adopted);
        petRepository.save(pet);


        applicationEventPublisher.publishEvent(
                NotificationEvent.builder()
                        .recipientUserId(userId)
                        .recipientEmail(username)
                        .message("Your Pet " + pet.getName() + " is now marked as adopted")
                        .type(NotificationType.PET_UPDATED).build()
        );


        return PetInfoPrivateResponse.builder()
                .id(pet.getId())
                .name(pet.getName())
                .type(pet.getType())
                .breed(pet.getBreed())
                .location(pet.getLocation())
                .adopted(pet.isAdopted())
                .owner(user.getEmail())
                .description(pet.getDescription())
                .build();


    }

    @Transactional
    public PetInfoPrivateResponse updatePetPost(UpdatePetRequest request, long petId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        Long userId = user.getId();
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new PetListingNotFoundException("Listing not found:" + petId));

        if (!pet.getOwner().getEmail().equals(user.getEmail())) {
            throw new UnauthorizedUserException("Pet Listing is not owned by User");
        }

        applyUpdates(pet, request);

        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            // remove old images safely
            List<ImageUrl> oldImageUrls = pet.getImages();
            oldImageUrls.forEach(img -> cloudinaryService.deleteFile(img.getPublicId()));

            pet.getImages().clear();
            List<ImageUrl> newImages = new ArrayList<>();
            for (Map<String, String> map : request.getImageUrls()) {
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    newImages.add(ImageUrl.builder()
                            .pet(pet)
                            .url(entry.getKey())
                            .publicId(entry.getValue())
                            .build());
                }
            }
            pet.getImages().addAll(newImages);
        }

        petRepository.save(pet);

        applicationEventPublisher.publishEvent(
                NotificationEvent.builder()
                        .recipientUserId(userId)
                        .recipientEmail(username)
                        .message("Your Pet Listing " + pet.getName() + " was successfully updated ! Changes are now live.")
                        .type(NotificationType.PET_UPDATED).build()
        );

        return PetInfoPrivateResponse.builder()
                .id(pet.getId())
                .name(pet.getName())
                .type(pet.getType())
                .breed(pet.getBreed())
                .location(pet.getLocation())
                .imageUrls(pet.getImages().stream().map(img -> {
                    Map<String, String> map = new HashMap<>();
                    map.put(img.getUrl(), img.getPublicId());
                    return map;
                }).toList())
                .adopted(pet.isAdopted())
                .owner(user.getEmail())
                .description(pet.getDescription())
                .build();
    }

    private void applyUpdates(Pet pet, UpdatePetRequest request) {
        if (request.getName() != null) pet.setName(request.getName());
        if (request.getType() != null) pet.setType(request.getType());
        if (request.getBreed() != null) pet.setBreed(request.getBreed());
        if (request.getLocation() != null) pet.setLocation(request.getLocation());
        if (request.getGender() != null) pet.setGender(Gender.valueOf(request.getGender()));
        if (request.getDescription() != null) pet.setDescription(request.getDescription());
        if (request.getAdopted() != null) pet.setAdopted(request.getAdopted());
    }
}


