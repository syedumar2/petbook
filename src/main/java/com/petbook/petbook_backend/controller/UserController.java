package com.petbook.petbook_backend.controller;


import com.petbook.petbook_backend.dto.request.AddPetRequest;
import com.petbook.petbook_backend.dto.request.UpdatePetRequest;
import com.petbook.petbook_backend.dto.request.UpdateUserRequest;
import com.petbook.petbook_backend.dto.response.ApiResponse;
import com.petbook.petbook_backend.dto.response.PetInfoPrivateResponse;
import com.petbook.petbook_backend.dto.response.UserDetailsResponse;
import com.petbook.petbook_backend.dto.response.UserInfoResponse;
import com.petbook.petbook_backend.models.User;
import com.petbook.petbook_backend.service.CloudinaryService;
import com.petbook.petbook_backend.service.PetService;
import com.petbook.petbook_backend.service.UserServiceImpl;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final PetService petService;
    private final CloudinaryService cloudinaryService;
    private final UserServiceImpl userService;

    @GetMapping("/user/me")
    public ResponseEntity<ApiResponse<UserInfoResponse>> userEndpoint() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userService.findByEmail(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Profile Details received", UserInfoResponse.builder()
                .email(userDetails.getUsername())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .roles(userDetails.getAuthorities())
                .createdAt(user.getCreatedAt())
                .profileImageUrl(user.getProfileImageUrl())
                .location(user.getLocation())
                .build()));
    }

    @GetMapping("/user/me/pets")
    public ResponseEntity<ApiResponse<List<PetInfoPrivateResponse>>> userPets() {
        List<PetInfoPrivateResponse> list = petService.getUserPets();
        return ResponseEntity.ok(ApiResponse.success("Pet Listings owned by you", list));

    }


    @PostMapping(value = "/user/me/pets", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<PetInfoPrivateResponse>> addPet(@Valid @RequestPart("petData") AddPetRequest request, @Valid @RequestPart("images") List<MultipartFile> images) {

        List<String> imageUrls = images.stream().map(cloudinaryService::uploadFile).collect(Collectors.toList());
        request.setImageUrls(imageUrls);


        PetInfoPrivateResponse response = petService.addPetPost(request);
        return ResponseEntity.ok(ApiResponse.success("Pet listed successfully", response));

    }

    @PutMapping(value = "/user/me/pets/{petId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<PetInfoPrivateResponse>> updatePet(@PathVariable @NotNull long petId,
                                                                         @Valid @RequestPart("petData") UpdatePetRequest request,
                                                                         @RequestPart(value = "images", required = false) List<MultipartFile> images) {

        if (images != null && !images.isEmpty()) {
            List<String> imageUrls = images.stream()
                    .map(cloudinaryService::uploadFile)
                    .collect(Collectors.toList());
            request.setImageUrls(imageUrls);
        }

        PetInfoPrivateResponse response = petService.updatePetPost(request, petId);
        return ResponseEntity.ok(ApiResponse.success("Pet listing updated successfully", response));
    }


    @DeleteMapping("/user/me/pets/{petId}")
    public ResponseEntity<ApiResponse<PetInfoPrivateResponse>> deletePet(@PathVariable @NotNull long petId) {
        PetInfoPrivateResponse response = petService.deletePetPost(petId);

        return ResponseEntity.ok(ApiResponse.success("Pet listing deleted successfully", response));

    }

    //Profile update controller
    @PatchMapping(value = "/user/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UserDetailsResponse>> updateUser(@RequestPart("userData") UpdateUserRequest request,
                                                                       @RequestPart(value = "imageUrl", required = false) MultipartFile image
                                                                    ){

        if (image != null && !image.isEmpty() ) {
            String imageUrls = cloudinaryService.uploadFile(image);
            request.setProfileImageUrl(imageUrls);
        }
        UserDetailsResponse response = userService.updateUser(request);
        return ResponseEntity.ok(ApiResponse.success("Updated Profile Data",response));
    }



}

//ALL ENDPOINTS WORKING AS EXPECTED ✅
