package com.petbook.petbook_backend.dto.request;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    String firstname;
    String lastname;
    String email;
    String password;
    String location;
}
