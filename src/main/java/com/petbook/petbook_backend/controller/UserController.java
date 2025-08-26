package com.petbook.petbook_backend.controller;


import com.petbook.petbook_backend.dto.request.AddPetRequest;
import com.petbook.petbook_backend.dto.request.UpdatePetRequest;
import com.petbook.petbook_backend.dto.request.UpdateUserRequest;
import com.petbook.petbook_backend.dto.response.ApiResponse;
import com.petbook.petbook_backend.dto.response.PageResponse;
import com.petbook.petbook_backend.dto.response.PetInfoPrivateResponse;
import com.petbook.petbook_backend.dto.response.PetInfoPublicResponse;
import com.petbook.petbook_backend.dto.response.UserDetailsResponse;
import com.petbook.petbook_backend.dto.response.UserInfoResponse;
import com.petbook.petbook_backend.models.User;
import com.petbook.petbook_backend.service.CloudinaryService;
import com.petbook.petbook_backend.service.PetService;
import com.petbook.petbook_backend.service.UserServiceImpl;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final PetService petService;
    private final CloudinaryService cloudinaryService;
    private final UserServiceImpl userService;

    @GetMapping("auth/user/me")
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

    @GetMapping("auth/user/me/pets")
    public ResponseEntity<ApiResponse<PageResponse<PetInfoPrivateResponse>>> userPets(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "sortField", defaultValue = "name") String sortField,
            @RequestParam(value = "sortDirection", defaultValue = "asc") String sortDirection

    ) {
        PageResponse<PetInfoPrivateResponse> pets = petService.getUserPets(sortField, sortDirection, page, size);
        return ResponseEntity.ok(ApiResponse.successWithCount(
                pets.getPageSize(),
                "Pet listings owned by you",
                pets
        ));

    }


    @PostMapping(value = "auth/user/me/pets", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<PetInfoPrivateResponse>> addPet(@Valid @RequestPart("petData") AddPetRequest request, @Valid @RequestPart("images") List<MultipartFile> images) {


            List<Map<String,String>> imageUrls = images.stream()
                    .map(cloudinaryService::uploadFile).collect(Collectors.toList());
            request.setImageUrls(imageUrls);


        PetInfoPrivateResponse response = petService.addPetPost(request);
        return ResponseEntity.ok(ApiResponse.success("Pet listed successfully", response));

    }

    @GetMapping("auth/user/me/pets/{petId}")
    public ResponseEntity<ApiResponse<PetInfoPrivateResponse>> getUserPetById(@PathVariable @NotNull long petId){
        PetInfoPrivateResponse response = petService.findUserPet(petId);
        return ResponseEntity.ok(ApiResponse.success("Pet listing retrieved successfully", response));
    }


    @DeleteMapping("auth/user/me/pets/{petId}")
    public ResponseEntity<ApiResponse<PetInfoPrivateResponse>> deletePet(@PathVariable @NotNull long petId) {
        PetInfoPrivateResponse response = petService.deletePetPost(petId);

        return ResponseEntity.ok(ApiResponse.success("Pet listing deleted successfully", response));

    }

    @PutMapping(value = "auth/user/me/pets/{petId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<PetInfoPrivateResponse>> updatePet(@PathVariable @NotNull long petId,
                                                                         @Valid @RequestPart("petData") UpdatePetRequest request,
                                                                         @RequestPart(value = "images", required = false) List<MultipartFile> images) {

        if (images != null && !images.isEmpty()) {
            List<Map<String,String>> imageUrls = images.stream()
                    .map(cloudinaryService::uploadFile).collect(Collectors.toList());
            request.setImageUrls(imageUrls);
        }

        PetInfoPrivateResponse response = petService.updatePetPost(request, petId);
        return ResponseEntity.ok(ApiResponse.success("Pet listing updated successfully", response));
    }

    //Profile update controller
    @PatchMapping(value = "auth/user/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UserDetailsResponse>> updateUser(@RequestPart("userData") UpdateUserRequest request,
                                                                       @RequestPart(value = "imageUrl", required = false) MultipartFile image
    ) {

        if (image != null && !image.isEmpty()) {
            Map<String, String> imageUrls  = cloudinaryService.uploadFile(image);
            for(Map.Entry map : imageUrls.entrySet()){
                request.setProfileImageUrl(map.getKey().toString());
                request.setPublicId(map.getValue().toString());
            }
        }
        UserDetailsResponse response = userService.updateUser(request);
        return ResponseEntity.ok(ApiResponse.success("Updated Profile Data", response));
    }


}

/*
ALL ENDPOINTS WORKING AS EXPECTED ✅
TODO Build a separate endpoint for changing password
TODO DONE : Refactor all endpoints that interact with cloudinary to delete any images on request or failure
Services that require cloudinary file deletion:
Profile update endpoint
Pet Listing update endpoint
Pet listing Delete endpoint
*/

