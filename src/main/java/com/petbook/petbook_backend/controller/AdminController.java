package com.petbook.petbook_backend.controller;

import com.petbook.petbook_backend.dto.request.PetActionRequest;
import com.petbook.petbook_backend.dto.request.UserActionsRequest;
import com.petbook.petbook_backend.dto.response.ApiResponse;
import com.petbook.petbook_backend.dto.response.PetInfoPrivateResponse;
import com.petbook.petbook_backend.dto.response.UserDetailsResponse;
import com.petbook.petbook_backend.service.AdminService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @PostMapping("/pets/approve")
    public ApiResponse<List<PetInfoPrivateResponse>> approvePet(@PathVariable @NotNull PetActionRequest request) {
        List<PetInfoPrivateResponse> petList = adminService.approvePet(request);
        return ApiResponse.successWithCount(petList.size(), "Pets approved successfully", petList);
    }

    @GetMapping("/users/blacklisted")
    public ApiResponse<List<UserDetailsResponse>> getBlackListedUsers() {
        List<UserDetailsResponse> users = adminService.getBlackListedUsers();
        return ApiResponse.successWithCount(users.size(), "Blacklisted Users fetched successfully", users);
    }

    @PostMapping("/users/blacklist")
    public ApiResponse<List<UserDetailsResponse>> blackListUsers(@RequestBody @NotNull UserActionsRequest request) {

        List<UserDetailsResponse> blackListedUsersList = adminService.blackListUsers(request);
        return ApiResponse.successWithCount(blackListedUsersList.size(), "Following Users were blacklisted", blackListedUsersList);


    }

    @PostMapping("/users/blacklist/{userId}")
    public ApiResponse<String> blackListUser(@PathVariable @NotNull Long userId, String message) {
        adminService.blackListUser(userId, message);
        return ApiResponse.success("User " + userId + " was blacklisted successfully", null);
    }

    @PostMapping("/pets/reject")
    public ApiResponse<List<PetInfoPrivateResponse>> rejectPet(@RequestBody @NotNull PetActionRequest request) {
        List<PetInfoPrivateResponse> petList = adminService.rejectPet(request);
        return ApiResponse.successWithCount(petList.size(), "Pets approved successfully", petList);

    }


    @GetMapping("/users")
    public ApiResponse<List<UserDetailsResponse>> getAllUsers() {
        List<UserDetailsResponse> users = adminService.getAllUsers();
        return ApiResponse.successWithCount(users.size(), "Users fetched successfully", users);
    }
}


//TODO Build delete user feature
//TODO build a send message to user feature
//TODO notify user if their listing was approved this willl tie into the sendMessage feature
//for now all current version api endpoints behave as expected ✅

