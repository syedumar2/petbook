package com.petbook.petbook_backend.dto.response;

import com.petbook.petbook_backend.models.Role;
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
}
