package com.petbook.petbook_backend.dto.response;

import lombok.*;

import java.util.Collection;
import java.util.Map;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoResponse {
    private String email;
    private Collection<?> roles;
}
