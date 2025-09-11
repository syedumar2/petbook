package com.petbook.petbook_backend.controller;


import com.petbook.petbook_backend.dto.request.FindPetByExampleRequest;
import com.petbook.petbook_backend.dto.response.ApiResponse;
import com.petbook.petbook_backend.dto.response.PageResponse;
import com.petbook.petbook_backend.dto.response.PetInfoPublicResponse;
import com.petbook.petbook_backend.service.PetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PetController {
    private final PetService petService;


    @GetMapping("/pets/getById/{id}")
    public ResponseEntity<ApiResponse<PetInfoPublicResponse>> getPetById(@PathVariable Long id) {
        PetInfoPublicResponse petInfo = petService.getPetById(id);
        return ResponseEntity.ok(ApiResponse.success("Pet Details for given pet id", petInfo));
    }



    @GetMapping("/pets/get/page-sort")
    public ResponseEntity<ApiResponse<PageResponse<PetInfoPublicResponse>>> getPetsWithPaginationAndSorting(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "sortField", defaultValue = "name") String sortField,
            @RequestParam(value = "sortDirection", defaultValue = "asc") String sortDirection
    ) {
        PageResponse<PetInfoPublicResponse> pets = petService.getPetsWithPaginationAndSorting(page, size, sortField, sortDirection);
        return ResponseEntity.ok(ApiResponse.successWithCount(
                pets.getPageSize(),
                "Pet listing with Pagination and Sorting",
                pets
        ));
    }


    @GetMapping("/pets/search")
    public ResponseEntity<ApiResponse<PageResponse<PetInfoPublicResponse>>> searchPets(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String breed,
            @RequestParam(required = false) String location,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "sortField", defaultValue = "name") String sortField,
            @RequestParam(value = "sortDirection", defaultValue = "asc") String sortDirection

    ) {
        PageResponse<PetInfoPublicResponse> pets = petService.searchPets(name, type, breed, location, page, size, sortField, sortDirection);
        return ResponseEntity.ok(ApiResponse.successWithCount(
                pets.getPageSize(),
                "Pet listing with Pagination and Sorting",
                pets
        ));
    }


    @PostMapping("/pets/get")
    public ResponseEntity<ApiResponse<PageResponse<PetInfoPublicResponse>>> findPetsByExample(@RequestBody FindPetByExampleRequest request,
                                                                                              @RequestParam(value = "page", defaultValue = "0") int page,
                                                                                              @RequestParam(value = "size", defaultValue = "20") int size,
                                                                                              @RequestParam(value = "sortField", defaultValue = "name") String sortField,
                                                                                              @RequestParam(value = "sortDirection", defaultValue = "asc") String sortDirection) {
        PageResponse<PetInfoPublicResponse> pets = petService.findPetsByExample(request,page,size,sortField,sortDirection);
        return ResponseEntity.ok(ApiResponse.successWithCount(
                pets.getPageSize(),
                "Pet listing with Pagination and Sorting",
                pets));
    }

    @GetMapping("/pets/autocomplete")
    public List<String> autocomplete(
            @RequestParam String field,
            @RequestParam String value) {
        return petService.autocomplete(field, value);
    }


}
//all endpoints in this PetController working as expected ✅