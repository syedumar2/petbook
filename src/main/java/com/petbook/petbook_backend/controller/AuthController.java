package com.petbook.petbook_backend.controller;


import com.petbook.petbook_backend.dto.request.LoginRequest;
import com.petbook.petbook_backend.dto.request.RefreshTokenRequest;
import com.petbook.petbook_backend.dto.request.RegisterRequest;
import com.petbook.petbook_backend.dto.response.ApiResponse;
import com.petbook.petbook_backend.dto.response.AuthResponse;
import com.petbook.petbook_backend.service.AuthService;
import com.petbook.petbook_backend.service.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;


    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> loginEndpoint(@RequestBody LoginRequest request, HttpServletResponse response) {

        AuthResponse authResponse = authService.login(request);
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", authResponse.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(7 * 24 * 60 * 60)
                .sameSite("Strict")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        return ResponseEntity.ok(ApiResponse.success("Logged in",AuthResponse.builder().token(authResponse.getToken()).build()));

    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> registerEndpoint(@RequestBody RegisterRequest request) {
        String response = authService.register(request);
        return ResponseEntity.ok(ApiResponse.success("Success",response));

    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(HttpServletRequest request) {
        String refreshToken = null;
        for (Cookie cookie : request.getCookies()) {
            if ("refreshToken".equals(cookie.getName())) {
                refreshToken = cookie.getValue();
                break;
            }
        }
        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
        return ResponseEntity.ok(ApiResponse.success("New token issued",authService.refreshAccessToken(refreshToken)));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            HttpServletRequest request,
            HttpServletResponse response
    ){
        authService.logout(request,response);
        return ResponseEntity.ok("Logged out successfully");

    }

}
