package com.petbook.petbook_backend.controller;


import com.petbook.petbook_backend.dto.request.AddPetRequest;
import com.petbook.petbook_backend.dto.response.ApiResponse;
import com.petbook.petbook_backend.dto.response.PageResponse;
import com.petbook.petbook_backend.dto.response.PetInfoResponse;
import com.petbook.petbook_backend.models.Pet;
import com.petbook.petbook_backend.service.PetService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PetController {
    private final PetService petService;


    @PostMapping("/pets")
    public ResponseEntity<ApiResponse<PetInfoResponse>> addPet(@RequestBody AddPetRequest request) {
        PetInfoResponse response = petService.addPetPost(request);
        return ResponseEntity.ok(ApiResponse.success("Pet listed successfully", response));

    }

    @GetMapping("/pets/get")
    public ResponseEntity<ApiResponse<List<PetInfoResponse>>> getAllPets() {
        List<PetInfoResponse> list = petService.getAllPets();
        return ResponseEntity.ok(ApiResponse.successWithCount(list.size(), "All Pets listed in System", list));

    }

    @GetMapping("/pets/get/{field}")
    public ResponseEntity<ApiResponse<List<PetInfoResponse>>> getPetswithSort(@PathVariable String field) {
        List<PetInfoResponse> list = petService.getPetsWithSorting(field);
        return ResponseEntity.ok(ApiResponse.successWithCount(list.size(), "Sorted Pet listing", list));

    }

    @GetMapping("/pets/getpage")
    public ResponseEntity<ApiResponse<PageResponse<PetInfoResponse>>> getPetswithPagination(@RequestParam(value = "page", defaultValue = "0") int page, @RequestParam(value = "size", defaultValue = "20") int size

    ) {
        PageResponse<PetInfoResponse> pets = petService.getPetsWithPagination(page, size);
        return ResponseEntity.ok(ApiResponse.successWithCount(pets.getPageSize(), "Pet listing with Pagination", pets));

    }


}
