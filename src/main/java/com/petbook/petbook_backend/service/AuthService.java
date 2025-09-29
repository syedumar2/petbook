package com.petbook.petbook_backend.service;

import com.petbook.petbook_backend.dto.request.LoginRequest;
import com.petbook.petbook_backend.dto.request.RegisterRequest;
import com.petbook.petbook_backend.dto.response.AuthResponse;
import com.petbook.petbook_backend.exceptions.rest.UnauthorizedUserException;
import com.petbook.petbook_backend.models.Notification;
import com.petbook.petbook_backend.models.NotificationType;
import com.petbook.petbook_backend.models.Role;
import com.petbook.petbook_backend.models.User;
import com.petbook.petbook_backend.repository.BlacklistedUserRepository;
import com.petbook.petbook_backend.repository.NotificationRepository;
import com.petbook.petbook_backend.repository.UserRepository;
import com.petbook.petbook_backend.service.events.NotificationEvent;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.aspectj.weaver.ast.Not;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.security.auth.RefreshFailedException;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final BlacklistedUserRepository blacklistedUserRepository;
    private final NotificationRepository notificationRepository;

    @Transactional
    public String register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already in use");
        }
        User user = new User();
        user.setFirstname(request.getFirstname());
        user.setLastname(request.getLastname());
        user.setEmail(request.getEmail());
        user.setLocation(request.getLocation());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setRole(Role.USER);


        user = userRepository.save(user);
        userRepository.flush();
        Notification notification = Notification.builder()
                .recipientId(user.getId())
                .message("Welcome to PetBook, " + user.getLastname() +
                        "! Start exploring adorable pets and connect with fellow animal lovers today!")
                .type(NotificationType.WELCOME)
                .createdAt(LocalDateTime.now())
                .isRead(false)
                .build();
        notificationRepository.save(notification);



        return "User registered successfully";

    }

    public String adminRegister(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already in use");
        }
        User user = new User();
        user.setFirstname(request.getFirstname());
        user.setLastname(request.getLastname());
        user.setEmail(request.getEmail());
        user.setLocation(request.getLocation());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setRole(Role.ADMIN);

        userRepository.save(user);
        return "Admin registered successfully";

    }

    public AuthResponse login(LoginRequest request) {

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));


        } catch (AuthenticationException e) {
            throw new RuntimeException(e.getMessage());
        }

        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new UsernameNotFoundException("Username not found in Db"));
        if (blacklistedUserRepository.existsByUserId(user.getId())) {
            throw new UnauthorizedUserException("User is blacklisted");
        }

        String jwtToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        user.setRefreshToken(refreshToken);
        userRepository.save(user);
        return AuthResponse.builder().token(jwtToken).refreshToken(refreshToken).build();
    }


    public AuthResponse refreshAccessToken(String refreshToken) throws RefreshFailedException {

        Long userId = jwtService.extractUserId(refreshToken);


        var user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));


        if (!jwtService.isTokenValid(refreshToken, user)) {  // optional: overload to skip UserDetails check
            throw new RefreshFailedException("Invalid Refresh Token");
        }


        String newAccessToken = jwtService.generateToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);


        user.setRefreshToken(newRefreshToken);
        userRepository.save(user);

        return AuthResponse.builder()
                .token(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }


    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractRefreshTokenFromCookie(request);
        if (refreshToken == null) {
            return;
        }
        try {
            String userEmail = jwtService.extractUsername(refreshToken);
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            user.setRefreshToken(null);
            userRepository.save(user);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
                    .httpOnly(true)
                    .secure(true) // set true only in production HTTPS
                    .path("/")
                    .maxAge(0)
                    .sameSite("None")
                    .build();
            response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());
        }

    }

    private String extractRefreshTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }


}


