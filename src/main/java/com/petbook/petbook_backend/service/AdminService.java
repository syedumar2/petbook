package com.petbook.petbook_backend.service;

import com.petbook.petbook_backend.dto.request.PetActionRequest;
import com.petbook.petbook_backend.dto.request.UserActionsRequest;
import com.petbook.petbook_backend.dto.response.PetInfoPrivateResponse;
import com.petbook.petbook_backend.dto.response.UserDetailsResponse;
import com.petbook.petbook_backend.exceptions.rest.UserNotFoundException;
import com.petbook.petbook_backend.models.BlacklistedUser;
import com.petbook.petbook_backend.models.NotificationType;
import com.petbook.petbook_backend.models.Pet;
import com.petbook.petbook_backend.models.User;
import com.petbook.petbook_backend.repository.BlacklistedUserRepository;
import com.petbook.petbook_backend.repository.PetRepository;
import com.petbook.petbook_backend.repository.UserRepository;
import com.petbook.petbook_backend.service.events.NotificationEvent;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final PetRepository petRepository;
    private final BlacklistedUserRepository blacklistedUserRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public List<PetInfoPrivateResponse> getApprovedPets() {
        return petRepository.findByApproved(true)
                .stream()
                .map(this::mapToPetInfoPrivateResponse)
                .toList();
    }

    @Transactional
    public List<PetInfoPrivateResponse> getUnapprovedPets() {
        return petRepository.findByApproved(false)
                .stream()
                .map(this::mapToPetInfoPrivateResponse)
                .toList();
    }

    @Transactional
    public List<PetInfoPrivateResponse> approvePet(PetActionRequest request) {
        List<Pet> pets = petRepository.findAllById(request.getIds());

        if (pets.size() != request.getIds().size()) {
            throw new RuntimeException("Some IDs are invalid");
        }
        List<Long> alreadyApproved = pets.stream()
                .filter(Pet::isApproved)
                .map(Pet::getId)
                .toList();

        if (!alreadyApproved.isEmpty()) {
            throw new IllegalArgumentException("These pets are already approved: " + alreadyApproved);
        }

        for (Pet pet : pets) {
            pet.setApproved(true);
            pet.setApprovedAt(LocalDateTime.now());
            pet.setRejectedAt(null);
        }

        pets = petRepository.saveAll(pets);
        for (Pet pet : pets) {
            applicationEventPublisher.publishEvent(NotificationEvent.builder()
                    .recipientEmail(pet.getOwner().getEmail())
                    .recipientUserId(pet.getOwner().getId())
                    .message("Your pet " + pet.getName() + " has been approved by Admin")
                    .type(NotificationType.PET_APPROVED)
                    .build());
        }
        return mapToListPetInfoPrivateResponse(pets);
    }

    @Transactional
    public List<PetInfoPrivateResponse> rejectPet(PetActionRequest request) {
        List<Pet> pets = petRepository.findAllById(request.getIds());

        if (pets.size() != request.getIds().size()) {
            throw new RuntimeException("Some IDs are invalid");
        }

        // Check if any are already rejected
        List<Long> alreadyRejected = pets.stream()
                .filter(p -> p.getRejectedAt() != null)
                .map(Pet::getId)
                .toList();

        if (!alreadyRejected.isEmpty()) {
            throw new IllegalArgumentException("These pets are already rejected: " + alreadyRejected);
        }

        // Mark pets as rejected
        for (Pet pet : pets) {
            pet.setRejectedAt(LocalDateTime.now());
            pet.setApproved(false);
            pet.setApprovedAt(null);
        }

        pets = petRepository.saveAll(pets);

        // Publish notification for each owner
        for (Pet pet : pets) {
            applicationEventPublisher.publishEvent(NotificationEvent.builder()
                    .recipientUserId(pet.getOwner().getId())
                    .message("Your pet " + pet.getName() + " has been rejected by Admin")
                    .type(NotificationType.PET_REJECTED)
                    .build());
        }

        return mapToListPetInfoPrivateResponse(pets);
    }

    @Transactional
    public List<UserDetailsResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToUserInfoResponse)
                .toList();
    }

    @Transactional
    public List<UserDetailsResponse> getBlackListedUsers() {
        return blacklistedUserRepository.findAll().stream().map(u -> {
            User user = u.getUser();
            return mapToUserInfoResponse(user);
        }).collect(Collectors.toList());
    }


    public List<UserDetailsResponse> blackListUsers(@NotNull UserActionsRequest request) {

        List<User> users = userRepository.findAllById(request.getIds());
        if (users.size() != request.getIds().size()) {
            throw new RuntimeException("Some IDs are invalid");
        }
        Map<Long, Boolean> alreadyBlackListed = users.stream()
                .collect(Collectors.toMap(
                        User::getId,                          // key = userId
                        u -> blacklistedUserRepository.existsByUserId(u.getId()) // value = isBlacklisted
                ));
        for (Map.Entry<Long, Boolean> pair : alreadyBlackListed.entrySet()) {
            if (pair.getValue()) {
                throw new RuntimeException("User is already blacklisted: " + pair.getKey());
            }
        }
        //success route
        List<BlacklistedUser> blacklistedUsersList = users.stream().map(u -> BlacklistedUser.builder()
                .user(u)
                .reason("You have been blacklisted by Admin")
                .blacklistedAt(LocalDateTime.now())
                .build()).toList();
        blacklistedUserRepository.saveAll(blacklistedUsersList);
        return mapToUserInfoResponseList(users);


    }

    @Transactional
    public void blackListUser(@NotNull Long userId, String message) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException("Invalid User id passed")
        );
        BlacklistedUser blacklistedUser = BlacklistedUser.builder()
                .user(user)
                .reason(message)
                .blacklistedAt(LocalDateTime.now())
                .build();
        blacklistedUserRepository.save(blacklistedUser);


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
                .approvedAt(pet.getApprovedAt())
                .rejectedAt(pet.getRejectedAt())
                .build();
    }

    private List<PetInfoPrivateResponse> mapToListPetInfoPrivateResponse(List<Pet> pets) {
        return pets.stream().map(this::mapToPetInfoPrivateResponse).collect(Collectors.toList());
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

    private List<UserDetailsResponse> mapToUserInfoResponseList(List<User> users) {
        return users.stream()
                .map(this::mapToUserInfoResponse)
                .toList();
    }
}
//TODO allow blacklisting and deleting users