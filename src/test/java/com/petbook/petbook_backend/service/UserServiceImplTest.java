package com.petbook.petbook_backend.service;

import com.petbook.petbook_backend.models.User;
import com.petbook.petbook_backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;


import java.util.Optional;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.awaitility.Awaitility.given;
import static org.mockito.Mockito.when;
import static org.mockito.BDDMockito.*;


@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void loadUserByUsername() {
        //given
        String email = "user@example.com";
        User user = new User();
        user.setEmail(email);
        user.setPassword("password");
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        //when
        UserDetails userDetails = userService.loadUserByUsername(email);


        //then
        assertThat(user).isEqualTo(userDetails);
        Mockito.verify(userRepository).findByEmail(email);


    }

    @Test
    void userDoesntExistCheck() {
        //given
        String email = "user@example.com";
        User user = new User();
        user.setEmail(email);
        user.setPassword("password");
        BDDMockito.given(userRepository.findByEmail(email)).willReturn(Optional.empty());

        assertThatThrownBy(()->userService.loadUserByUsername(email))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found");

    }
}