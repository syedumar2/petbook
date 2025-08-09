package com.petbook.petbook_backend.controller;

import com.petbook.petbook_backend.dto.request.FindPetByExampleRequest;
import com.petbook.petbook_backend.dto.response.PageResponse;
import com.petbook.petbook_backend.dto.response.PetInfoPublicResponse;
import com.petbook.petbook_backend.security.SecurityConfig;
import com.petbook.petbook_backend.service.JwtService;
import com.petbook.petbook_backend.service.PetService;
import com.petbook.petbook_backend.service.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(value = PetController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
// disables Spring Boot’s default security config

@Import(SecurityConfig.class)
@AutoConfigureMockMvc(addFilters = false)
//Disables the Spring Security filters (like JWT auth filters).
class PetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PetService petService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserServiceImpl userService;


    @BeforeEach
    void setUp() {


    }

    @Test
    void getAllPetsApi() throws Exception {
        //given
        List<PetInfoPublicResponse> petList = List.of(
                new PetInfoPublicResponse() {{
                    setName("Buddy");
                    setType("Dog");
                    setBreed("Golden Retriever");
                    setLocation("Bangalore");
                    setImageUrls(List.of("https://example.com/buddy1.jpg", "https://example.com/buddy2.jpg"));
                    setAdopted(false);
                    setOwner("alice@example.com");
                    setDescription("Friendly and energetic golden retriever looking for a loving home.");
                }},
                new PetInfoPublicResponse() {{
                    setName("Whiskers");
                    setType("Cat");
                    setBreed("Siamese");
                    setLocation("Mumbai");
                    setImageUrls(List.of("https://example.com/whiskers1.jpg"));
                    setAdopted(true);
                    setOwner("bob@example.com");
                    setDescription("Calm and affectionate Siamese cat, already adopted.");
                }},
                new PetInfoPublicResponse() {{
                    setName("Rocky");
                    setType("Dog");
                    setBreed("Bulldog");
                    setLocation("Delhi");
                    setImageUrls(List.of("https://example.com/rocky.jpg"));
                    setAdopted(false);
                    setOwner("charlie@example.com");
                    setDescription("Loyal bulldog great with kids and other pets.");
                }}
        );

        given(petService.getAllPets()).willReturn(petList);

        //when
        //then
        mockMvc.perform(get("/api/pets/get"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("All Pets listed in System"))
                .andExpect(jsonPath("$.recordCount").value(3))


                .andExpect(jsonPath("$.data[0].name").value("Buddy"))
                .andExpect(jsonPath("$.data[0].type").value("Dog"))

                .andExpect(jsonPath("$.data[1].name").value("Whiskers"))
                .andExpect(jsonPath("$.data[1].type").value("Cat"));


    }

    @Test
    void getAllPets_whenNoPetsExist_shouldReturnEmptyList() throws Exception {
        given(petService.getAllPets()).willReturn(List.of());

        mockMvc.perform(get("/api/pets/get"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("All Pets listed in System"))
                .andExpect(jsonPath("$.recordCount").value(0))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void getAllPets_withNullFields_shouldHandleGracefully() throws Exception {
        PetInfoPublicResponse petWithNulls = new PetInfoPublicResponse();
        petWithNulls.setName("Ghost");
        petWithNulls.setType(null); // intentionally null
        petWithNulls.setBreed("Unknown");
        petWithNulls.setLocation("Nowhere");
        petWithNulls.setImageUrls(null);
        petWithNulls.setAdopted(false);
        petWithNulls.setOwner(null);
        petWithNulls.setDescription(null);

        given(petService.getAllPets()).willReturn(List.of(petWithNulls));

        mockMvc.perform(get("/api/pets/get"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.recordCount").value(1))
                .andExpect(jsonPath("$.data[0].name").value("Ghost"))
                .andExpect(jsonPath("$.data[0].type").doesNotExist());
    }

    @Test
    void getAllPets_whenServiceReturnsEmptyList() throws Exception {
        String errorMsg = "Something went wrong in service";
        given(petService.getAllPets()).willReturn(new ArrayList<PetInfoPublicResponse>());

        mockMvc.perform(get("/api/pets/get/name"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.recordCount").value(0))
                .andExpect(jsonPath("$.data").isEmpty());
    }


    @Test
    void getPetswithSort() throws Exception {
        //given
        String field = "name";
        List<PetInfoPublicResponse> mockPets = List.of(
                new PetInfoPublicResponse() {{
                    setName("Alpha");
                    setType("Dog");
                    setBreed("Golden Retriever");
                    setLocation("Bangalore");
                    setImageUrls(List.of("https://example.com/buddy1.jpg", "https://example.com/buddy2.jpg"));
                    setAdopted(false);
                    setOwner("alice@example.com");
                    setDescription("Friendly and energetic golden retriever looking for a loving home.");
                }},
                new PetInfoPublicResponse() {{
                    setName("Bravo");
                    setType("Cat");
                    setBreed("Siamese");
                    setLocation("Mumbai");
                    setImageUrls(List.of("https://example.com/whiskers1.jpg"));
                    setAdopted(true);
                    setOwner("bob@example.com");
                    setDescription("Calm and affectionate Siamese cat, already adopted.");
                }}
        );
        given(petService.getPetsWithSorting(field)).willReturn(mockPets);
        mockMvc.perform(get("/api/pets/get/{field}", field))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.recordCount").value(2))
                .andExpect(jsonPath("$.message").value("Sorted Pet listing"))
                .andExpect(jsonPath("$.data[0].name").value("Alpha"))
                .andExpect(jsonPath("$.data[1].name").value("Bravo"));


    }

    @Test
    void getPetswithSort_withInvalidFieldReturnsException() throws Exception {
        //given
        String field = "wrongField";

        //when
        given(petService.getPetsWithSorting(field)).willThrow(new IllegalArgumentException("Invalid sort field: " + field));
        //then
        mockMvc.perform(get("/api/pets/get/{field}", field))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid sort field: " + field));


    }

    @Test
    void getPetswithPagination_shouldReturnPaginatedPets() throws Exception {
        // Given
        int page = 0;
        int size = 2;

        List<PetInfoPublicResponse> pets = List.of(
                PetInfoPublicResponse.builder()
                        .name("Alpha")
                        .type("Dog")
                        .breed("Golden Retriever")
                        .location("Bangalore")
                        .imageUrls(List.of("url1", "url2"))
                        .adopted(false)
                        .owner("alice@example.com")
                        .description("Friendly dog")
                        .build(),
                PetInfoPublicResponse.builder()
                        .name("Bravo")
                        .type("Cat")
                        .breed("Siamese")
                        .location("Mumbai")
                        .imageUrls(List.of("url3"))
                        .adopted(true)
                        .owner("bob@example.com")
                        .description("Adopted cat")
                        .build()
        );
        PageResponse<PetInfoPublicResponse> mockPageResponse = new PageResponse<>();
        mockPageResponse.setContent(pets);
        mockPageResponse.setPageNumber(page);
        mockPageResponse.setPageSize(size);
        mockPageResponse.setTotalPages(1);
        mockPageResponse.setTotalElements(2);

        given(petService.getPetsWithPagination(page, size)).willReturn(mockPageResponse);

        // When & Then
        mockMvc.perform(get("/api/pets/get/page")
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.recordCount").value(size))
                .andExpect(jsonPath("$.message").value("Pet listing with Pagination"))
                .andExpect(jsonPath("$.data.content[0].name").value("Alpha"))
                .andExpect(jsonPath("$.data.content[1].name").value("Bravo"))
                .andExpect(jsonPath("$.data.totalElements").value(2)).andDo(print());
    }


    @Test
    void searchPets_shouldReturnFilteredPets() throws Exception {
        // Given
        String name = "Leo";
        String type = "Dog";
        String breed = "Labrador";
        String location = "Delhi";

        List<PetInfoPublicResponse> mockPets = List.of(
                PetInfoPublicResponse.builder()
                        .name("Leo")
                        .type("Dog")
                        .breed("Labrador")
                        .location("Delhi")
                        .imageUrls(List.of("url1"))
                        .adopted(false)
                        .owner("leo@example.com")
                        .description("Very loyal dog")
                        .build()
        );

        given(petService.searchPets(name, type, breed, location)).willReturn(mockPets);

        // When & Then
        mockMvc.perform(get("/api/pets/search")
                        .param("name", name)
                        .param("type", type)
                        .param("breed", breed)
                        .param("location", location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.recordCount").value(1))
                .andExpect(jsonPath("$.message").value("Search successful"))
                .andExpect(jsonPath("$.data[0].name").value("Leo"))
                .andExpect(jsonPath("$.data[0].type").value("Dog"))
                .andExpect(jsonPath("$.data[0].breed").value("Labrador"))
                .andExpect(jsonPath("$.data[0].location").value("Delhi"));
    }

    @Test
    void searchPets_withPartialParams_shouldStillWork() throws Exception {
        List<PetInfoPublicResponse> mockPets = List.of(
                PetInfoPublicResponse.builder()
                        .name("Coco")
                        .type("Cat")
                        .breed("Persian")
                        .location("Mumbai")
                        .imageUrls(List.of("url1"))
                        .adopted(false)
                        .owner("coco@example.com")
                        .description("Chill cat")
                        .build()
        );

        given(petService.searchPets(null, "Cat", null, "Mumbai")).willReturn(mockPets);

        mockMvc.perform(get("/api/pets/search")
                        .param("type", "Cat")
                        .param("location", "Mumbai"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.recordCount").value(1))
                .andExpect(jsonPath("$.message").value("Search successful"))
                .andExpect(jsonPath("$.data[0].name").value("Coco"));
    }

    @Test
    void findPetsByExample_shouldReturnMatchingPets() throws Exception {
        FindPetByExampleRequest request = new FindPetByExampleRequest();
        request.setType("dog");
        List<PetInfoPublicResponse> mockResponse = List.of(
                new PetInfoPublicResponse("Leo", "dog", "Labrador", "Delhi", List.of("url1", "url2"), false, "john@example.com", "Friendly dog")
        );

        // Mock service
        when(petService.findPetsByExample(any())).thenReturn(mockResponse);

        // Perform POST with DTO as JSON
        mockMvc.perform(post("/api/pets/get")
                        .content(new ObjectMapper().writeValueAsString(request))
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Query successful"))
                .andExpect(jsonPath("$.recordCount").value(1))
                .andExpect(jsonPath("$.data[0].name").value("Leo"));

    }

}