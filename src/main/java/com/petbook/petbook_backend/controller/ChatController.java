package com.petbook.petbook_backend.controller;

import com.petbook.petbook_backend.dto.request.ChatMessageRequest;
import com.petbook.petbook_backend.dto.request.ConversationRequest;
import com.petbook.petbook_backend.dto.request.MarkReadRequest;
import com.petbook.petbook_backend.dto.response.ApiResponse;
import com.petbook.petbook_backend.dto.response.ConversationResponse;
import com.petbook.petbook_backend.dto.response.MessageResponse;
import com.petbook.petbook_backend.dto.sockets.SocketEvent;
import com.petbook.petbook_backend.models.CustomUserDetails;
import com.petbook.petbook_backend.service.ChatService;
import com.petbook.petbook_backend.service.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ChatController {
    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);
    private final ChatService chatService;
    private final UserServiceImpl userService;

    @PostMapping("/api/auth/chat/start")
    public ResponseEntity<ApiResponse<ConversationResponse>> startConversation(
            @RequestParam Long user1Id,
            @RequestParam Long user2Id,
            @RequestParam(required = false) Long petId) {
        ConversationRequest request = new ConversationRequest(user1Id, user2Id, petId);
        ConversationResponse response = chatService.startConversation(request);
        return ResponseEntity.ok(ApiResponse.success("Conversation started", response));
    }

    @DeleteMapping("/api/auth/chat/delete/{conversationId}")
    public ResponseEntity<ApiResponse<ConversationResponse>> endConversation(@PathVariable Long conversationId) {
        ConversationResponse response = chatService.deleteConversation(conversationId);
        return ResponseEntity.ok(ApiResponse.success("Conversation deleted", response));
    }

    @GetMapping("/api/auth/chat/getMyConversations")
    public ResponseEntity<ApiResponse<List<ConversationResponse>>> getMyConversations(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<ConversationResponse> responseList = chatService.getUserConversations(userDetails);
        return ResponseEntity.ok(ApiResponse.success("Retrieved Conversations", responseList));
    }

    @GetMapping("/api/auth/chat/get/{conversationId}")
    public ResponseEntity<ApiResponse<ConversationResponse>> getMyConversation(
            @PathVariable Long conversationId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        ConversationResponse response = chatService.getConversation(conversationId, userDetails);
        return ResponseEntity.ok(ApiResponse.success("Retrieved Conversation", response));
    }

    // WebSocket: Send Message
    @MessageMapping("/chat.sendMessage")
    public void sendMessage(SocketEvent<ChatMessageRequest> request, Message<?> message) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        if (accessor.getUser() == null || request.getPayload() == null) return;

        UsernamePasswordAuthenticationToken token =
                (UsernamePasswordAuthenticationToken) accessor.getUser();
        CustomUserDetails userDetails = (CustomUserDetails) token.getPrincipal();
        if (userDetails.getId() == null) return;

        chatService.sendMessage(request.getPayload(), userDetails.getId());
    }

    // WebSocket: Mark Messages as Read
    @MessageMapping("/chat.markRead")
    public void markMessagesAsRead(SocketEvent<MarkReadRequest> request, Message<?> message) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        if (accessor.getUser() == null || request.getPayload() == null) return;

        UsernamePasswordAuthenticationToken token =
                (UsernamePasswordAuthenticationToken) accessor.getUser();
        CustomUserDetails userDetails = (CustomUserDetails) token.getPrincipal();
        if (userDetails.getId() == null) return;

        chatService.markAsRead(request.getPayload(), userDetails.getId());
    }

    // REST: Get messages for a conversation
    @GetMapping("/api/auth/chat/{conversationId}/messages")
    public ResponseEntity<ApiResponse<List<MessageResponse>>> getMessages(
            @PathVariable Long conversationId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long authenticatedUserId = userDetails.getId();
        List<MessageResponse> response = chatService.getMessages(conversationId, authenticatedUserId);
        return ResponseEntity.ok(ApiResponse.success("All Messages retrieved", response));
    }
}
