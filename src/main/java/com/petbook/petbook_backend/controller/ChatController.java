package com.petbook.petbook_backend.controller;


import com.petbook.petbook_backend.dto.request.ChatMessageRequest;
import com.petbook.petbook_backend.dto.request.ConversationRequest;
import com.petbook.petbook_backend.dto.request.MarkReadRequest;
import com.petbook.petbook_backend.dto.response.*;
import com.petbook.petbook_backend.service.ChatService;
import com.petbook.petbook_backend.service.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;
    private final UserServiceImpl userService;

    @PostMapping("/api/chat/start")
    public ResponseEntity<ApiResponse<ConversationResponse>> startConversation(@RequestParam Long user1Id,
                                                                               @RequestParam Long user2Id,
                                                                               @RequestParam(required = false) Long petId)
    {
        ConversationRequest request = new ConversationRequest(user1Id,user2Id,petId);
        ConversationResponse response = chatService.startConversation(request);
        return ResponseEntity.ok(ApiResponse.success("Conversation started",response));

    }
    @MessageMapping("/chat.sendMessage")
    public void sendMessage(ChatMessageRequest request, Principal principal)
    {
        String username = principal.getName();

        // Fetch the authenticated user's ID from the DB
        Long authenticatedUserId = userService.findUserId(username);
        chatService.sendMessage(request, authenticatedUserId);


    }
    @MessageMapping("/chat.markRead")
    public MarkReadResponse markMessagesAsRead(MarkReadRequest request,Principal principal) {
        String username = principal.getName();

        // Fetch the authenticated user's ID from the DB
        Long authenticatedUserId = userService.findUserId(username);
        List<Long> list = chatService.markAsRead(request,authenticatedUserId);
        return new MarkReadResponse(list);

    }

    @GetMapping("/api/chat/{conversationId}/messages")
    public ResponseEntity<ApiResponse<List<MessageResponse>>> getMessages(
            @PathVariable Long conversationId, Principal principal) {
        String username = principal.getName();

        // Fetch the authenticated user's ID from the DB
        Long authenticatedUserId = userService.findUserId(username);
        List<MessageResponse> response = chatService.getMessages(conversationId, authenticatedUserId);
        return ResponseEntity.ok(ApiResponse.success("All Messages retrieved",response));
    }
}



//ChatController working as expected ✅