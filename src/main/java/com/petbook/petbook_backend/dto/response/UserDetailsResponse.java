package com.petbook.petbook_backend.dto.response;

import com.petbook.petbook_backend.models.Role;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public class UserDetailsResponse {
    Long id;
    String firstname;
    String lastname;
    String email;
    String location;
    String profileImageUrl;
    Role role;
    LocalDateTime createdAt;
}
