package com.petbook.petbook_backend.service;

import com.petbook.petbook_backend.dto.request.UpdateUserRequest;
import com.petbook.petbook_backend.dto.response.UserDetailsResponse;
import com.petbook.petbook_backend.exceptions.UserNotFoundException;
import com.petbook.petbook_backend.models.User;
import com.petbook.petbook_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Primary
@RequiredArgsConstructor
public class UserServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username).orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    public UserDetailsResponse updateUser(UpdateUserRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(username).orElseThrow(() -> new UserNotFoundException("User not found"));
        if (request.getFirstname() != null) user.setFirstname(request.getFirstname());
        if (request.getLastname() != null) user.setLastname(request.getLastname());
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            userRepository.findByEmail(request.getEmail()).ifPresent(user1 -> {
                if (!user1.getId().equals(user.getId())) {
                    throw new DuplicateKeyException("Email already in use");
                }
            });
            user.setEmail(request.getEmail());
        }

        if (request.getPassword() != null)
            user.setPassword(passwordEncoder.encode(request.getPassword())); // Consider hashing this!
        if (request.getLocation() != null) user.setLocation(request.getLocation());
        if (request.getProfileImageUrl() != null) user.setProfileImageUrl(request.getProfileImageUrl());
        ;
        User newUserDetails = userRepository.save(user);
        return UserDetailsResponse.builder().firstname(newUserDetails.getFirstname()).lastname(newUserDetails.getLastname()).email(newUserDetails.getEmail()).location(newUserDetails.getLocation()).profileImageUrl(newUserDetails.getProfileImageUrl()).build();

    }
}
