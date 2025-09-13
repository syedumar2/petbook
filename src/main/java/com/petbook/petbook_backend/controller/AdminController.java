package com.petbook.petbook_backend.controller;

import com.petbook.petbook_backend.dto.request.PetActionRequest;
import com.petbook.petbook_backend.dto.request.UserActionsRequest;
import com.petbook.petbook_backend.dto.response.ApiResponse;
import com.petbook.petbook_backend.dto.response.PageResponse;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/pets/approved")
    public ApiResponse<PageResponse<PetInfoPrivateResponse>> getApprovedPets(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "sortField", defaultValue = "name") String sortField,
            @RequestParam(value = "sortDirection", defaultValue = "asc") String sortDirection
    ) {
        PageResponse<PetInfoPrivateResponse> pets = adminService.getApprovedPets(page, size, sortField, sortDirection);
        return ApiResponse.successWithCount(pets.getPageSize(), "Approved pets fetched successfully", pets);
    }

    @GetMapping("/pets/unapproved")
    public ApiResponse<PageResponse<PetInfoPrivateResponse>> getUnapprovedPets(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "sortField", defaultValue = "name") String sortField,
            @RequestParam(value = "sortDirection", defaultValue = "asc") String sortDirection
    ) {
        PageResponse<PetInfoPrivateResponse> pets = adminService.getUnapprovedPets(page, size, sortField, sortDirection);
        return ApiResponse.successWithCount(pets.getPageSize(), "Unapproved pets fetched successfully", pets);

    }

    @GetMapping("/pets")
    public ApiResponse<PageResponse<PetInfoPrivateResponse>> getAllPets(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "sortField", defaultValue = "name") String sortField,
            @RequestParam(value = "sortDirection", defaultValue = "asc") String sortDirection)
    {
        PageResponse<PetInfoPrivateResponse> pets = adminService.getAllPets(page, size, sortField, sortDirection);
        return ApiResponse.successWithCount(pets.getPageSize(), "All pets fetched successfully", pets);
    }



    @PostMapping("/pets/approve")
    public ApiResponse<List<PetInfoPrivateResponse>> approvePet(@RequestBody @NotNull PetActionRequest request) {
        List<PetInfoPrivateResponse> petList = adminService.approvePet(request);
        return ApiResponse.successWithCount(petList.size(), "Pets approved successfully", petList);
    }

    @GetMapping("/users/blacklisted")
    public ApiResponse<PageResponse<UserDetailsResponse>> getBlackListedUsers(@RequestParam(value = "page", defaultValue = "0") int page,
                                                                              @RequestParam(value = "size", defaultValue = "20") int size,
                                                                              @RequestParam(value = "sortField", defaultValue = "blacklistedAt") String sortField,
                                                                              @RequestParam(value = "sortDirection", defaultValue = "asc") String sortDirection) {
        PageResponse<UserDetailsResponse> users = adminService.getBlackListedUsers(
                page, size, sortField, sortDirection);
        return ApiResponse.successWithCount(users.getPageSize(), "Blacklisted Users fetched successfully", users);
    }

    @PostMapping("/users/blacklist")
    public ApiResponse<List<UserDetailsResponse>> blackListUsers(@RequestBody @NotNull UserActionsRequest request) {

        List<UserDetailsResponse> blackListedUsersList = adminService.blackListUsers(request);
        return ApiResponse.successWithCount(blackListedUsersList.size(), "Following Users were blacklisted", blackListedUsersList);


    }

    @PostMapping("/users/blacklist/{userId}")
    public ApiResponse<String> blackListUser(@PathVariable @NotNull Long userId,
                                             @RequestBody @NotNull String message) {
        adminService.blackListUser(userId, message);
        return ApiResponse.success("User " + userId + " was blacklisted successfully", null);
    }

    @PostMapping("/users/whitelist/{userId}")
    public ApiResponse<String> whiteListUser(@PathVariable @NotNull Long userId) {
        adminService.whiteListUser(userId);
        return ApiResponse.success("User " + userId + " was whitelisted successfully", null);

    }

    @PostMapping("/pets/reject")
    public ApiResponse<List<PetInfoPrivateResponse>> rejectPet(@RequestBody @NotNull PetActionRequest request) {
        List<PetInfoPrivateResponse> petList = adminService.rejectPet(request);
        return ApiResponse.successWithCount(petList.size(), "Pets were rejected successfully", petList);

    }


    @GetMapping("/users")
    public ApiResponse<PageResponse<UserDetailsResponse>> getAllUsers(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "sortField", defaultValue = "createdAt") String sortField,
            @RequestParam(value = "sortDirection", defaultValue = "asc") String sortDirection
    ) {
        PageResponse<UserDetailsResponse> users = adminService.getAllUsers(page, size, sortField, sortDirection);
        return ApiResponse.successWithCount(users.getPageSize(), "Users fetched successfully", users);
    }
}


//for now all current version api endpoints behave as expected ✅

