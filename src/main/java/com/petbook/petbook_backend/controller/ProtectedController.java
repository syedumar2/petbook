package com.petbook.petbook_backend.controller;


import com.petbook.petbook_backend.dto.response.ApiResponse;
import com.petbook.petbook_backend.dto.response.PetInfoResponse;
import com.petbook.petbook_backend.dto.response.UserInfoResponse;
import com.petbook.petbook_backend.service.PetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<ApiResponse<List<PetInfoResponse>>> userPets(){
        List<PetInfoResponse> list = petService.getUserPets();
        return ResponseEntity.ok(ApiResponse.success("Pet Listings owned by you",list));

    }

}
