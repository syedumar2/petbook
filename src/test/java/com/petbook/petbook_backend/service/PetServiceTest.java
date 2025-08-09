package com.petbook.petbook_backend.service;

import com.petbook.petbook_backend.dto.request.AddPetRequest;
import com.petbook.petbook_backend.dto.request.UpdatePetRequest;
import com.petbook.petbook_backend.dto.response.PageResponse;
import com.petbook.petbook_backend.dto.response.PetInfoPrivateResponse;
import com.petbook.petbook_backend.dto.response.PetInfoPublicResponse;
import com.petbook.petbook_backend.models.Pet;
import com.petbook.petbook_backend.models.User;
import com.petbook.petbook_backend.repository.PetRepository;
import com.petbook.petbook_backend.repository.UserRepository;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.BeforeEach;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


@ExtendWith(MockitoExtension.class)
class PetServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PetRepository petRepository;
    @InjectMocks
    private PetService underTest;


    @BeforeEach
    void setUp() {

    }

    @Test
    void addPetPost() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user@example.com");

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);

        SecurityContextHolder.setContext(securityContext);
        //given


        String email = "user@example.com";
        User user = new User();
        user.setEmail(email);

        AddPetRequest request = new AddPetRequest();
        request.setName("Leo");
        request.setType("Dog");
        request.setBreed("Labrador");
        request.setLocation("Delhi");
        request.setImageUrls(new ArrayList<>(Arrays.asList("https://image.com/leo.jpg", "https://image.com/leo.jpg")));

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        Pet savedPet = new Pet();
        savedPet.setId(1L);
        savedPet.setName("Leo");
        savedPet.setType("Dog");
        savedPet.setBreed("Labrador");
        savedPet.setLocation("Delhi");
        savedPet.setImageUrls(new ArrayList<>(Arrays.asList("https://image.com/leo.jpg", "https://image.com/leo.jpg")));
        savedPet.setOwner(user);
        savedPet.setAdopted(false);

        BDDMockito.when(petRepository.save(any(Pet.class))).thenReturn(savedPet);

        //when
        PetInfoPrivateResponse response = underTest.addPetPost(request);

        //then
        assertNotNull(response);
        assertEquals(savedPet.getId(), response.getId(), "Pet id if given should match");
        assertEquals(savedPet.getId(), response.getId(), "Pet ID should match");
        assertEquals(savedPet.getName(), response.getName(), "Pet name should match");
        assertEquals(savedPet.getType(), response.getType(), "Pet type should match");
        assertEquals(savedPet.getBreed(), response.getBreed(), "Pet breed should match");
        assertEquals(savedPet.getLocation(), response.getLocation(), "Pet location should match");
        assertEquals(savedPet.getImageUrls(), response.getImageUrls(), "Pet image URL should match");
        assertEquals(savedPet.isAdopted(), response.isAdopted(), "Pet adopted status should match");
        assertEquals(user.getEmail(), response.getOwner(), "Pet owner email should match");

        //get the captured state of the petRepo save function and compare it with the savedPet
        ArgumentCaptor<Pet> argumentCaptor = ArgumentCaptor.forClass(Pet.class);
        verify(petRepository).save(argumentCaptor.capture());
        Pet capturedPet = argumentCaptor.getValue();


        assertEquals(savedPet.getName(), capturedPet.getName());
        assertEquals(savedPet.getType(), capturedPet.getType());
        assertEquals(savedPet.getBreed(), capturedPet.getBreed());
        assertEquals(savedPet.getLocation(), capturedPet.getLocation());
        assertEquals(savedPet.getImageUrls(), capturedPet.getImageUrls());
        assertEquals(savedPet.getOwner(), capturedPet.getOwner());


        verify(userRepository).findByEmail(email);


    }

    @Test
    void addPetPostButUserNotFoundCheck() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user@example.com");

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);

        SecurityContextHolder.setContext(securityContext);
        //given

        AddPetRequest request = new AddPetRequest();
        String email = "user@example.com";
        User user = new User();
        user.setEmail(email);

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        //when
        //then
        assertThatThrownBy(() -> underTest.addPetPost(request)).isInstanceOf(RuntimeException.class).hasMessage("User not found");
        verify(petRepository, never()).save(any());


    }

    @Test
    void getAllPets() {
        //given

        User user = new User();
        Pet pet1 = Pet.builder().id(1L).name("Doggo").type("Dog").breed("Labrador").location("Hyderabad").imageUrls(new ArrayList<>(List.of("url1"))).adopted(false).owner(user).build();
        Pet pet2 = Pet.builder().id(2L).name("Kitty").type("Cat").breed("Persian").location("Delhi").imageUrls(new ArrayList<>(List.of("url2"))).adopted(true).owner(user).build();
        List<Pet> mockPets = List.of(pet1, pet2);
        when(petRepository.findAll()).thenReturn(mockPets);
        //when
        List<PetInfoPublicResponse> list = underTest.getAllPets();
        //then
        assertEquals(mockPets.get(0).getName(), list.get(0).getName());
        assertEquals(mockPets.get(0).getType(), list.get(0).getType());
        assertEquals(mockPets.get(0).getLocation(), list.get(0).getLocation());
        assertEquals(mockPets.get(1).getName(), list.get(1).getName());
        assertEquals(mockPets.get(1).getType(), list.get(1).getType());
        assertEquals(mockPets.get(1).getLocation(), list.get(1).getLocation());
        verify(petRepository).findAll();

    }

    @Test
    void getUserPets() {
        //given
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user@example.com");

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);

        SecurityContextHolder.setContext(securityContext);

        String email = "user@example.com";
        User user = new User();
        user.setEmail(email);
        user.setId(1L);

        Pet pet1 = Pet.builder().id(1L).name("Doggo").type("Dog").breed("Labrador").location("Hyderabad").imageUrls(new ArrayList<>(List.of("url1"))).adopted(false).owner(user).build();
        Pet pet2 = Pet.builder().id(2L).name("Kitty").type("Cat").breed("Persian").location("Delhi").imageUrls(new ArrayList<>(List.of("url2"))).adopted(true).owner(user).build();


        //when
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(petRepository.findByOwnerId(1L)).thenReturn(List.of(pet1, pet2));


        //then
        List<PetInfoPrivateResponse> responseList = underTest.getUserPets();
        assertEquals(2, responseList.size());
        assertEquals("Doggo", responseList.get(0).getName());
        assertEquals("Kitty", responseList.get(1).getName());
        assertEquals("user@example.com", responseList.get(0).getOwner());
        verify(petRepository).findByOwnerId(1L);


    }

    @Test
    void getPetsWithSorting() {
        // given
        String sortField = "name";
        User user1 = new User();
        user1.setEmail("example@gmail.com");

        User user2 = new User();
        user2.setEmail("example1@gmail.com");

        Pet pet1 = new Pet();
        pet1.setName("Alpha");
        pet1.setType("Dog");
        pet1.setBreed("Labrador");
        pet1.setLocation("Delhi");
        pet1.setImageUrls(new ArrayList<>(List.of("img1.jpg")));
        pet1.setAdopted(false);
        pet1.setOwner(user1); // adjust constructor as needed

        Pet pet2 = new Pet();
        pet2.setName("Bravo");
        pet2.setType("Cat");
        pet2.setBreed("Persian");
        pet2.setLocation("Mumbai");
        pet2.setImageUrls(new ArrayList<>(List.of("img2.jpg")));
        pet2.setAdopted(true);
        pet2.setOwner(user2);

        List<Pet> mockPets = List.of(pet1, pet2);
        BDDMockito.given(petRepository.findAll(Sort.by(Sort.Direction.ASC, sortField))).willReturn(mockPets);


        //when
        List<PetInfoPublicResponse> pets = underTest.getPetsWithSorting(sortField);

        //then
        verify(petRepository).findAll(Sort.by(Sort.Direction.ASC, sortField));
        assertEquals(pet1.getName(), pets.get(0).getName());
        assertEquals(pet2.getName(), pets.get(1).getName());
        assertEquals(pet1.getOwner().getEmail(), pets.get(0).getOwner());
        assertEquals(pet2.getOwner().getEmail(), pets.get(1).getOwner());
        AssertionsForClassTypes.assertThat(pets.size()).isEqualTo(2);


    }

    @Test
    void getPetsWithPagination() {
        // given
        int page = 0;
        int size = 2;

        User user1 = new User();
        user1.setEmail("example@gmail.com");

        User user2 = new User();
        user2.setEmail("example1@gmail.com");

        Pet pet1 = new Pet();
        pet1.setName("Alpha");
        pet1.setType("Dog");
        pet1.setBreed("Labrador");
        pet1.setLocation("Delhi");
        pet1.setImageUrls(new ArrayList<>(List.of("img1.jpg")));
        pet1.setAdopted(false);
        pet1.setOwner(user1); // adjust constructor as needed

        Pet pet2 = new Pet();
        pet2.setName("Bravo");
        pet2.setType("Cat");
        pet2.setBreed("Persian");
        pet2.setLocation("Mumbai");
        pet2.setImageUrls(new ArrayList<>(List.of("img1.jpg")));
        pet2.setAdopted(true);
        pet2.setOwner(user2);

        Pageable pageable = PageRequest.of(page, size);
        List<Pet> petList = List.of(pet1, pet2);
        Page<Pet> mockPetPage = new PageImpl<>(petList, pageable, petList.size());

        Mockito.when(petRepository.findAll(pageable)).thenReturn(mockPetPage);
        //when
        PageResponse<PetInfoPublicResponse> petInfoPublicResponsePage = underTest.getPetsWithPagination(page, size);

        //then
        assertEquals(mockPetPage.getContent().get(0).getName(), petInfoPublicResponsePage.getContent().get(0).getName());
        assertThat(mockPetPage.getTotalElements()).isEqualTo(petInfoPublicResponsePage.getTotalElements());
        assertThat(mockPetPage.getSize()).isEqualTo(petInfoPublicResponsePage.getPageSize());
        assertThat(mockPetPage.getNumber()).isEqualTo(petInfoPublicResponsePage.getPageNumber());
        verify(petRepository).findAll(pageable);


    }

    @Test
    void emptyPageCheck() {
        // given
        int page = 0;
        int size = 2;


        Pageable pageable = PageRequest.of(page, size);
        List<Pet> petList = new ArrayList<>();
        Page<Pet> mockPetPage = new PageImpl<>(petList, pageable, 0);

        Mockito.when(petRepository.findAll(pageable)).thenReturn(mockPetPage);
        //when
        PageResponse<PetInfoPublicResponse> result = underTest.getPetsWithPagination(page, size);

        //then
        assertThat(result.getContent().size()).isEqualTo(0);
        AssertionsForClassTypes.assertThat(result.getPageSize()).isEqualTo(size);
        AssertionsForClassTypes.assertThat(result.getPageNumber()).isEqualTo(page);
        AssertionsForClassTypes.assertThat(result.getTotalElements()).isEqualTo(0);
        verify(petRepository).findAll(pageable);


    }

    @Test
    void updatePetPost() {
        long petId = 1L;
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user@example.com");

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);

        SecurityContextHolder.setContext(securityContext);
        //given


        String email = "user@example.com";
        User user = new User();
        user.setEmail(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        Pet pet = new Pet();
        pet.setId(petId);
        pet.setName("Leo");
        pet.setType("Dog");
        pet.setBreed("Labrador");
        pet.setLocation("Delhi");
        pet.setImageUrls(new ArrayList<>(Arrays.asList("https://image.com/leo.jpg", "https://image.com/leo.jpg")));
        pet.setOwner(user);
        pet.setAdopted(false);

        when(petRepository.findById(petId)).thenReturn(Optional.of(pet));

        UpdatePetRequest request = new UpdatePetRequest();
        request.setAdopted(true);
        request.setName("Bruno");


        // Stubbing repos
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(petRepository.findById(petId)).thenReturn(Optional.of(pet));
        when(petRepository.save(any(Pet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        //when
        PetInfoPrivateResponse response = underTest.updatePetPost(request, petId);

        //then
        assertThat(response.getId()).isEqualTo(petId);
        assertThat(response.getName()).isEqualTo("Bruno");
        assertThat(response.isAdopted()).isTrue();
        assertThat(response.getId()).isEqualTo(1L); // if you expect id to be 1L

    }

    @Test
    void updatePetPost_InvalidPetId() {
        long petId = 1L;
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user@example.com");

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);

        SecurityContextHolder.setContext(securityContext);
        //given


        String email = "user@example.com";
        User user = new User();
        user.setEmail(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(petRepository.findById(petId)).thenReturn(Optional.empty());
        UpdatePetRequest request = new UpdatePetRequest();
        request.setAdopted(true);
        request.setName("Bruno");


        //when
        //then
        assertThatThrownBy(() -> underTest.updatePetPost(request, petId)).isInstanceOf(RuntimeException.class).hasMessage("Listing not found");
        verify(petRepository, never()).save(any());
    }

    @Test
    void updatePetPost_InvalidUserId() {
        long petId = 1L;
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user@example.com");

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);

        SecurityContextHolder.setContext(securityContext);
        //given

        String email = "user@example.com";
        User user = new User();
        user.setEmail(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        UpdatePetRequest request = new UpdatePetRequest();
        request.setAdopted(true);
        request.setName("Bruno");


        //when
        //then
        assertThatThrownBy(() -> underTest.updatePetPost(request, petId)).isInstanceOf(RuntimeException.class).hasMessage("User not found");
        verify(petRepository, never()).save(any());
        verify(petRepository, never()).findById(any());
    }

    @Test
    void updatePetPost_UnauthorizedListingAccess() {
        long petId = 1L;
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user@example.com");

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);

        SecurityContextHolder.setContext(securityContext);
        //given


        String email = "user@example.com";
        User user = new User();
        user.setEmail(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        User user1 = new User();
        user1.setEmail("somethingelse@gmail.com");
        Pet savedPet = new Pet();
        savedPet.setId(petId);
        savedPet.setName("Leo");
        savedPet.setType("Dog");
        savedPet.setBreed("Labrador");
        savedPet.setLocation("Delhi");
        savedPet.setImageUrls(new ArrayList<>(Arrays.asList("https://image.com/leo.jpg", "https://image.com/leo.jpg")));
        savedPet.setOwner(user1);
        savedPet.setAdopted(false);

        when(petRepository.findById(petId)).thenReturn(Optional.of(savedPet));

        UpdatePetRequest request = new UpdatePetRequest();
        request.setAdopted(true);
        request.setName("Bruno");

        //when
        //then
        assertThatThrownBy(() -> underTest.updatePetPost(request, petId)).isInstanceOf(RuntimeException.class).hasMessage("Pet Listing is not owned by User");
        verify(petRepository, never()).save(any());

    }


    @Test
    void deletePetPost() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user@example.com");

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);

        SecurityContextHolder.setContext(securityContext);

        String email = "user@example.com";
        User user = new User();
        user.setEmail(email);

        Pet mockPet = new Pet();
        mockPet.setId(1L);
        mockPet.setName("Leo");
        mockPet.setType("Dog");
        mockPet.setBreed("Labrador");
        mockPet.setLocation("Delhi");
        mockPet.setImageUrls(new ArrayList<>(Arrays.asList("https://image.com/leo.jpg", "https://image.com/leo.jpg")));
        mockPet.setOwner(user);
        mockPet.setAdopted(false);

        //given
        long petId = 1L;
        BDDMockito.given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        BDDMockito.given(petRepository.findById(petId)).willReturn(Optional.of(mockPet));

        //when
        underTest.deletePetPost(petId);

        //then
        verify(petRepository).delete(mockPet);


    }

    @Test
    void deletePetPost_PostDoesNotBelongToUser() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user@example.com");

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);

        SecurityContextHolder.setContext(securityContext);

        String email = "user@example.com";
        User user = new User();
        user.setEmail(email);


        User user1 = new User();
        user1.setEmail("user2@example.com");

        Pet mockPet = new Pet();
        mockPet.setId(1L);
        mockPet.setName("Leo");
        mockPet.setType("Dog");
        mockPet.setBreed("Labrador");
        mockPet.setLocation("Delhi");
        mockPet.setImageUrls(new ArrayList<>(Arrays.asList("https://image.com/leo.jpg", "https://image.com/leo.jpg")));
        mockPet.setOwner(user1);
        mockPet.setAdopted(false);

        //given
        long petId = 1L;
        BDDMockito.given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
        BDDMockito.given(petRepository.findById(petId)).willReturn(Optional.of(mockPet));

        //when

        //then
        assertThatThrownBy(() -> underTest.deletePetPost(petId)).isInstanceOf(RuntimeException.class).hasMessage("Pet Listing is not owned by User");
        verify(petRepository, never()).delete(any());


    }


}