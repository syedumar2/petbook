package com.petbook.petbook_backend.dto.request;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserRequest {
    String firstname;
    String lastname;
    String email;
    String location;
    String profileImageUrl;
    String publicId;
}
