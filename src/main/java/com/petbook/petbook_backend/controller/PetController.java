package com.petbook.petbook_backend.controller;


import com.petbook.petbook_backend.dto.response.ApiResponse;
import com.petbook.petbook_backend.dto.response.PageResponse;
import com.petbook.petbook_backend.dto.response.PetInfoPublicResponse;
import com.petbook.petbook_backend.service.PetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PetController {
    private final PetService petService;



    @GetMapping("/pets/get")
    public ResponseEntity<ApiResponse<List<PetInfoPublicResponse>>> getAllPets() {
        List<PetInfoPublicResponse> list = petService.getAllPets();
        return ResponseEntity.ok(ApiResponse.successWithCount(list.size(), "All Pets listed in System", list));

    }

    @GetMapping("/pets/get/{field}")
    public ResponseEntity<ApiResponse<List<PetInfoPublicResponse>>> getPetswithSort(@PathVariable String field) {
        List<PetInfoPublicResponse> list = petService.getPetsWithSorting(field);
        return ResponseEntity.ok(ApiResponse.successWithCount(list.size(), "Sorted Pet listing", list));

    }

    @GetMapping("/pets/get/page")
    public ResponseEntity<ApiResponse<PageResponse<PetInfoPublicResponse>>> getPetswithPagination(@RequestParam(value = "page", defaultValue = "0") int page, @RequestParam(value = "size", defaultValue = "20") int size

    ) {
        PageResponse<PetInfoPublicResponse> pets = petService.getPetsWithPagination(page, size);
        return ResponseEntity.ok(ApiResponse.successWithCount(pets.getPageSize(), "Pet listing with Pagination", pets));

    }


}
