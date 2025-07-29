package com.petbook.petbook_backend.controller;


import com.petbook.petbook_backend.dto.request.AddPetRequest;
import com.petbook.petbook_backend.dto.request.UpdatePetRequest;
import com.petbook.petbook_backend.dto.response.ApiResponse;
import com.petbook.petbook_backend.dto.response.PetInfoPrivateResponse;
import com.petbook.petbook_backend.dto.response.UserInfoResponse;
import com.petbook.petbook_backend.service.PetService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProtectedController {

    private final PetService petService;

    @GetMapping("/user/me")
    public ResponseEntity<ApiResponse<UserInfoResponse>> userEndpoint() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok
                (
                        ApiResponse.success("Profile Details recieved",
                                new UserInfoResponse(userDetails.getUsername(), userDetails.getAuthorities())
                        )
                );
    }

    @GetMapping("/user/me/pets")
    public ResponseEntity<ApiResponse<List<PetInfoPrivateResponse>>> userPets() {
        List<PetInfoPrivateResponse> list = petService.getUserPets();
        return ResponseEntity.ok(ApiResponse.success("Pet Listings owned by you", list));

    }

    @PostMapping("/user/me/pets")
    public ResponseEntity<ApiResponse<PetInfoPrivateResponse>> addPet(@RequestBody AddPetRequest request) {
        PetInfoPrivateResponse response = petService.addPetPost(request);
        return ResponseEntity.ok(ApiResponse.success("Pet listed successfully", response));

    }

    @PutMapping("/user/me/pets/{petId}")
    public ResponseEntity<ApiResponse<PetInfoPrivateResponse>> updatePet(@PathVariable @NotNull long petId,
                                                                         @RequestBody UpdatePetRequest request) {

        PetInfoPrivateResponse response = petService.updatePetPost(request, petId);

        return ResponseEntity.ok(ApiResponse.success("Pet listing updated successfully", response));

    }

    @DeleteMapping("/user/me/pets/{petId}")
    public ResponseEntity<ApiResponse<PetInfoPrivateResponse>> deletePet(@PathVariable @NotNull long petId) {
        PetInfoPrivateResponse response = petService.deletePetPost(petId);

        return ResponseEntity.ok(ApiResponse.success("Pet listing deleted successfully", response));

    }


}


//TODO Build image upload feature
//TODO websockets
//TODO advanced search q
//TODO learn mokito
