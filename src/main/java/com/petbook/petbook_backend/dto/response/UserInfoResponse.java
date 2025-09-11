package com.petbook.petbook_backend.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInfoResponse {
    private Long id;
    private String email;
    private Collection<?> roles;
    private  String firstname;
    private String lastname;
    private String location;
    private LocalDateTime createdAt;
    private String profileImageUrl;
}
