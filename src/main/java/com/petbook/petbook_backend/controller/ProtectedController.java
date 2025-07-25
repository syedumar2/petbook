package com.petbook.petbook_backend.controller;


import com.petbook.petbook_backend.dto.response.UserInfoResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ProtectedController {
    @GetMapping("/user")
    public ResponseEntity<UserInfoResponse> userEndpoint() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(new UserInfoResponse(userDetails.getUsername(), userDetails.getAuthorities()));
    }

}
