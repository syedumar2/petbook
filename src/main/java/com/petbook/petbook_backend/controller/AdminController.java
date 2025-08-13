package com.petbook.petbook_backend.controller;

import com.petbook.petbook_backend.dto.response.ApiResponse;
import com.petbook.petbook_backend.dto.response.PetInfoPrivateResponse;
import com.petbook.petbook_backend.dto.response.UserDetailsResponse;
import com.petbook.petbook_backend.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/pets/approved")
    public ApiResponse<List<PetInfoPrivateResponse>> getApprovedPets() {
        List<PetInfoPrivateResponse> pets = adminService.getApprovedPets();
        return ApiResponse.successWithCount(pets.size(), "Approved pets fetched successfully", pets);
    }

    @GetMapping("/pets/unapproved")
    public ApiResponse<List<PetInfoPrivateResponse>> getUnapprovedPets() {
        List<PetInfoPrivateResponse> pets = adminService.getUnapprovedPets();
        return ApiResponse.successWithCount(pets.size(), "Unapproved pets fetched successfully", pets);
    }

    @PostMapping("/pets/{petId}/approve")
    public ApiResponse<PetInfoPrivateResponse> approvePet(@PathVariable Long petId) {
        PetInfoPrivateResponse pet = adminService.approvePet(petId);
        return ApiResponse.success("Pet approved successfully", pet);
    }

    @PostMapping("/pets/{petId}/reject")
    public ApiResponse<PetInfoPrivateResponse> rejectPet(@PathVariable Long petId) {
        PetInfoPrivateResponse pet = adminService.rejectPet(petId);
        return ApiResponse.success("Pet rejected successfully", pet);
    }

    @GetMapping("/users")
    public ApiResponse<List<UserDetailsResponse>> getAllUsers() {
        List<UserDetailsResponse> users = adminService.getAllUsers();
        return ApiResponse.successWithCount(users.size(), "Users fetched successfully", users);
    }
}


    //In the future build some admin endpoints to allow blacklisting users
    //allow deletion of users
    //for now all current version api endpoints behave as expected ✅

