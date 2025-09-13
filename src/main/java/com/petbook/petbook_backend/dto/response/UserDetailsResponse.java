package com.petbook.petbook_backend.dto.response;

import com.petbook.petbook_backend.models.Role;
import com.petbook.petbook_backend.models.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailsResponse {
    private Long id;
    private String firstname;
    private String lastname;
    private String email;
    private String location;
    private String profileImageUrl;
    private Role role;
    private LocalDateTime createdAt;
    private LocalDateTime blacklistedAt;


    public static UserDetailsResponse fromEntity(User user) {
        return UserDetailsResponse.builder()
                .id(user.getId())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .email(user.getEmail())
                .location(user.getLocation())
                .profileImageUrl(user.getProfileImageUrl())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }

    public static UserDetailsResponse fromEntityWithBlackList(User user, LocalDateTime blacklistedAt) {
        return UserDetailsResponse.builder()
                .id(user.getId())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .email(user.getEmail())
                .location(user.getLocation())
                .profileImageUrl(user.getProfileImageUrl())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .blacklistedAt(blacklistedAt)
                .build();
    }
}
