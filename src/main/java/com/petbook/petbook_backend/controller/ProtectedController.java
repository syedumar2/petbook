package com.petbook.petbook_backend.controller;


import com.petbook.petbook_backend.dto.request.AddPetRequest;
import com.petbook.petbook_backend.dto.request.UpdatePetRequest;
import com.petbook.petbook_backend.dto.response.ApiResponse;
import com.petbook.petbook_backend.dto.response.PetInfoPrivateResponse;
import com.petbook.petbook_backend.dto.response.UserInfoResponse;
import com.petbook.petbook_backend.service.CloudinaryService;
import com.petbook.petbook_backend.service.PetService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
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
public class ProtectedController {

    private final PetService petService;
    private final CloudinaryService cloudinaryService;

    @GetMapping("/user/me")
    public ResponseEntity<ApiResponse<UserInfoResponse>> userEndpoint() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(ApiResponse.success("Profile Details received", new UserInfoResponse(userDetails.getUsername(), userDetails.getAuthorities())));
    }

    @GetMapping("/user/me/pets")
    public ResponseEntity<ApiResponse<List<PetInfoPrivateResponse>>> userPets() {
        List<PetInfoPrivateResponse> list = petService.getUserPets();
        return ResponseEntity.ok(ApiResponse.success("Pet Listings owned by you", list));

    }

    //TODO Test validation
    @PostMapping(value = "/user/me/pets", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<PetInfoPrivateResponse>> addPet(@Valid @RequestPart("petData") AddPetRequest request, @Valid @RequestPart("images") List<MultipartFile> images) {

        List<String> imageUrls = images.stream().map(cloudinaryService::uploadFile).collect(Collectors.toList());
        request.setImageUrls(imageUrls);


        PetInfoPrivateResponse response = petService.addPetPost(request);
        return ResponseEntity.ok(ApiResponse.success("Pet listed successfully", response));

    }

    @PutMapping("/user/me/pets/{petId}")
    public ResponseEntity<ApiResponse<PetInfoPrivateResponse>> updatePet(@PathVariable @NotNull long petId, @RequestBody UpdatePetRequest request) {

        PetInfoPrivateResponse response = petService.updatePetPost(request, petId);

        return ResponseEntity.ok(ApiResponse.success("Pet listing updated successfully", response));

    }

    @DeleteMapping("/user/me/pets/{petId}")
    public ResponseEntity<ApiResponse<PetInfoPrivateResponse>> deletePet(@PathVariable @NotNull long petId) {
        PetInfoPrivateResponse response = petService.deletePetPost(petId);

        return ResponseEntity.ok(ApiResponse.success("Pet listing deleted successfully", response));

    }


}

//TODO write repo tests
//TODO write controller tests
//TODO Build image upload feature
//TODO websockets
//TODO advanced search q

