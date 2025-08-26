package com.petbook.petbook_backend.controller;

import com.petbook.petbook_backend.dto.request.AddPetRequest;
import com.petbook.petbook_backend.dto.request.UpdatePetRequest;
import com.petbook.petbook_backend.dto.response.PetInfoPrivateResponse;
import com.petbook.petbook_backend.service.CloudinaryService;
import com.petbook.petbook_backend.service.JwtService;
import com.petbook.petbook_backend.service.PetService;
import com.petbook.petbook_backend.service.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.List;

import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
public class ProtectedControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SecurityContext securityContext;

    @MockitoBean
    private PetService petService;

    @MockitoBean
    private CloudinaryService cloudinaryService;
    @MockitoBean
    private UserServiceImpl userService;

    @MockitoBean
    private JwtService jwtService;


    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    void userEndpoint_ReturnsUserInfo_Success() throws Exception {

        UserDetails userDetails = new User("testUser", "password", Collections.singletonList(() -> "ROLE_USER"));
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        mockMvc.perform(get("/api/user/me")).andExpect(status().isOk()).andExpect(jsonPath("$.message").value("Profile Details received")).andExpect(jsonPath("$.data.email").value("testUser")).andExpect(jsonPath("$.data.roles[0].authority").value("ROLE_USER"));
    }

//    @Test
//    @WithMockUser(username = "test@example.com", roles = {"USER"})
//    void userPets() throws Exception {
//        //given
//        UserDetails userDetails = new User("test@example.com", "password", Collections.singletonList(() -> "ROLE_USER"));
//        Authentication authentication = mock(Authentication.class);
//        when(authentication.getPrincipal()).thenReturn(userDetails);
//        when(securityContext.getAuthentication()).thenReturn(authentication);
//        SecurityContextHolder.setContext(securityContext);
//        List<PetInfoPrivateResponse> petList = List.of(PetInfoPrivateResponse.builder().id(1L).name("Leo").type("Dog").breed("Labrador").location("Hyderabad").imageUrls(List.of("https://example.com/leo1.jpg", "https://example.com/leo2.jpg")).adopted(false).description("Playful and loyal Labrador, loves to run.").owner("test@example.com").build(), PetInfoPrivateResponse.builder().id(2L).name("Milo").type("Cat").breed("Bengal").location("Chennai").imageUrls(List.of("https://example.com/milo.jpg")).adopted(true).description("Independent Bengal cat, already adopted.").owner("test@example.com").build(), PetInfoPrivateResponse.builder().id(3L).name("Bruno").type("Dog").breed("Beagle").location("Pune").imageUrls(List.of("https://example.com/bruno.jpg")).adopted(false).description("Friendly Beagle good with families.").owner("test@example.com").build());
//
//
//        when(petService.getUserPets()).thenReturn(petList);
//
//        mockMvc.perform(get("/api/user/me/pets"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.success").value(true))
//                .andExpect(jsonPath("$.message").value("Pet Listings owned by you"))
//                .andExpect(jsonPath("$.data[0].owner").value("test@example.com"))
//                .andExpect(jsonPath("$.data[1].owner").value("test@example.com"))
//                .andExpect(jsonPath("$.data[2].owner").value("test@example.com"));
//
//
//    }

    @Test
    void userEndpoint_Unauthenticated_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/user/me")).andExpect(status().isUnauthorized()); // 401
    }

    @Test
    void userPets_Unauthenticated_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/user/me/pets")).andExpect(status().isUnauthorized()); // 401
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    void addPet_Success() throws Exception {
        //given

        UserDetails userDetails = new User("test@example.com", "password", Collections.singletonList(() -> "ROLE_USER"));
        Authentication authentication = mock(Authentication.class);

        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // 1. Prepare petData as JSON
        AddPetRequest request = new AddPetRequest();
        request.setName("Coco");
        request.setType("Cat");
        request.setBreed("Persian");
        request.setLocation("Mumbai");
        request.setDescription("Chill Persian cat");

        String petDataJson = new ObjectMapper()
                .writeValueAsString(request);

        MockMultipartFile petDataPart = new MockMultipartFile(
                "petData", "petData.json", "application/json", petDataJson.getBytes()
        );

        // 2. Prepare mock image file(s)
        MockMultipartFile image1 = new MockMultipartFile(
                "images", "coco.jpg", MediaType.IMAGE_JPEG_VALUE, "image-content".getBytes()
        );

        List<String> mockImageUrls = List.of("https://mock.cloudinary.com/coco.jpg");

        // 3. Stub cloudinaryService and petService
        when(cloudinaryService.uploadFile(any())).thenReturn(mockImageUrls.get(0));

        PetInfoPrivateResponse response = PetInfoPrivateResponse.builder()
                .id(1L)
                .name("Coco")
                .type("Cat")
                .breed("Persian")
                .location("Mumbai")
                .description("Chill Persian cat")
                .adopted(false)
                .imageUrls(mockImageUrls)
                .owner("test@example.com")
                .build();

        when(petService.addPetPost(any(AddPetRequest.class))).thenReturn(response);

        // 4. Perform multipart request
        mockMvc.perform(multipart("/api/user/me/pets")
                        .file(petDataPart)
                        .file(image1)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                .with(csrf())
                .with(req-> { req.setMethod("POST"); return req; }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Pet listed successfully"))
                .andExpect(jsonPath("$.data.name").value("Coco"))
                .andExpect(jsonPath("$.data.imageUrls[0]").value("https://mock.cloudinary.com/coco.jpg"))
                .andExpect(jsonPath("$.data.owner").value("test@example.com"));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    void addPet_ValidationFailure_when_Image_is_Missing() throws Exception {
        //given

        UserDetails userDetails = new User("test@example.com", "password", Collections.singletonList(() -> "ROLE_USER"));
        Authentication authentication = mock(Authentication.class);

        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // 1. Prepare petData as JSON
        AddPetRequest request = new AddPetRequest();
        request.setName("Coco");
        request.setType("Cat");
        request.setBreed("Persian");
        request.setLocation("Mumbai");
        request.setDescription("Chill Persian cat");

        String petDataJson = new ObjectMapper()
                .writeValueAsString(request);


        MockMultipartFile petDataPart = new MockMultipartFile(
                "petData", "petData.json", "application/json", petDataJson.getBytes()
        );



        // 4. Perform multipart request
        mockMvc.perform(multipart("/api/user/me/pets")
                        .file(petDataPart)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(csrf())
                        .with(req-> { req.setMethod("POST"); return req; }))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Missing part: images"))
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    void addPet_ValidationFailure_when_Field_is_Missing() throws Exception {
        //given

        UserDetails userDetails = new User("test@example.com", "password", Collections.singletonList(() -> "ROLE_USER"));
        Authentication authentication = mock(Authentication.class);

        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);


        AddPetRequest request = new AddPetRequest();
        request.setBreed("Persian");
        request.setLocation("Mumbai");
        request.setDescription("Chill Persian cat");

        String petDataJson = new ObjectMapper()
                .writeValueAsString(request);


        MockMultipartFile petDataPart = new MockMultipartFile(
                "petData", "petData.json", "application/json", petDataJson.getBytes()
        );
        MockMultipartFile image1 = new MockMultipartFile(
                "images", "coco.jpg", MediaType.IMAGE_JPEG_VALUE, "image-content".getBytes()
        );

        List<String> mockImageUrls = List.of("https://mock.cloudinary.com/coco.jpg");


        when(cloudinaryService.uploadFile(any())).thenReturn(mockImageUrls.get(0));





        mockMvc.perform(multipart("/api/user/me/pets")
                        .file(petDataPart)
                        .file(image1)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(csrf())
                        .with(req-> { req.setMethod("POST"); return req; }))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Pet Data Validation failed"))
                .andExpect(jsonPath("$.data.name").value("Name is required"))
                .andExpect(jsonPath("$.data.type").value("Pet Type is required"));


    }



    @Test
    void updatePet_success() throws Exception {

        UserDetails userDetails = new User("test@example.com", "password", Collections.singletonList(() -> "ROLE_USER"));
        Authentication authentication = mock(Authentication.class);

        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // 1. Prepare petData as JSON
        UpdatePetRequest request = new UpdatePetRequest();
        request.setName("Rumi");
        request.setType("Cat");
        request.setDescription("Chill Persian cat");

        //we were here
        String petDataJson = new ObjectMapper()
                .writeValueAsString(request);

        MockMultipartFile petDataPart = new MockMultipartFile(
                "petData", "petData.json", "application/json", petDataJson.getBytes()
        );

        // 2. Prepare mock image file(s)
        MockMultipartFile image1 = new MockMultipartFile(
                "images", "coco.jpg", MediaType.IMAGE_JPEG_VALUE, "image-content".getBytes()
        );

        List<String> mockImageUrls = List.of("https://mock.cloudinary.com/coco.jpg");

        // 3. Stub cloudinaryService and petService
        when(cloudinaryService.uploadFile(any())).thenReturn(mockImageUrls.get(0));

        PetInfoPrivateResponse response = PetInfoPrivateResponse.builder()
                .id(1L)
                .name("Coco")
                .type("Cat")
                .breed("Persian")
                .location("Mumbai")
                .description("Chill Persian cat")
                .adopted(false)
                .imageUrls(mockImageUrls)
                .owner("test@example.com")
                .build();

        when(petService.addPetPost(any(AddPetRequest.class))).thenReturn(response);

        // 4. Perform multipart request
        mockMvc.perform(multipart("/api/user/me/pets")
                        .file(petDataPart)
                        .file(image1)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(csrf())
                        .with(req-> { req.setMethod("POST"); return req; }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Pet listed successfully"))
                .andExpect(jsonPath("$.data.name").value("Coco"))
                .andExpect(jsonPath("$.data.imageUrls[0]").value("https://mock.cloudinary.com/coco.jpg"))
                .andExpect(jsonPath("$.data.owner").value("test@example.com"));
    }


    @Test
    void updatePet_Failure_when_pet_Doesnt_Exist() {
    }
    @Test
    void updatePet_Failure_when_user_Doesnt_Exist() {
    }
    @Test
    void updatePet_Failure_when_pet_Doesnt_belong_to_user() {
    }

    @Test
    void deletePet() {
    }

    @Test
    void updateUser() {
    }
}