package com.petbook.petbook_backend.security;

import com.petbook.petbook_backend.models.CustomUserDetails;
import com.petbook.petbook_backend.service.JwtService;
import com.petbook.petbook_backend.service.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtChannelInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final UserServiceImpl userService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = accessor.getFirstNativeHeader("Authorization");

            log.debug("WebSocket CONNECT attempt. Raw token header: {}", token);

            if (token == null || !token.startsWith("Bearer ")) {
                log.warn("WebSocket CONNECT failed: Missing or invalid Authorization header");
                throw new IllegalArgumentException("Missing or invalid Authorization header");
            }
            token = token.substring(7);

            Long userId = jwtService.extractUserId(token);  // extract ID from token
            String username = jwtService.extractUsername(token);
            String role = jwtService.extractUserRole(token);
            System.out.println("Interceptor logging: "+" userId: " + userId +" username: " + username +" role: " + role);

            if (username == null || userId == null) {
                log.warn("WebSocket CONNECT failed: Invalid JWT token (no username)");
                throw new IllegalArgumentException("Invalid JWT token");
            }

            CustomUserDetails userDetails = new CustomUserDetails(userId, username, role);

            if (!jwtService.isTokenValid(token, userDetails)) {
                log.warn("WebSocket CONNECT failed: Token validation failed for user {}", username);
                throw new IllegalArgumentException("Invalid JWT token");
            }

            accessor.setUser(
                    new UsernamePasswordAuthenticationToken(
                            userDetails,      // <- principal is the full CustomUserDetails
                            null,
                            userDetails.getAuthorities()
                    )
            );
            log.info("WebSocket CONNECT successful for user: {}", username);
        }
        else if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
            if (accessor.getUser() != null) {
                log.info("WebSocket DISCONNECT for user: {}", accessor.getUser().getName());
            } else {
                log.info("WebSocket DISCONNECT for unknown user (possibly invalid connection)");
            }
        }
        return message;
    }
}
