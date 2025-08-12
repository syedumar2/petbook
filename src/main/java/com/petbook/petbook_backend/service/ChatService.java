package com.petbook.petbook_backend.service;

import com.petbook.petbook_backend.dto.request.ChatMessageRequest;
import com.petbook.petbook_backend.dto.request.ConversationRequest;
import com.petbook.petbook_backend.dto.request.MarkReadRequest;
import com.petbook.petbook_backend.dto.response.ChatMessageResponse;
import com.petbook.petbook_backend.dto.response.ConversationResponse;
import com.petbook.petbook_backend.dto.response.MessageResponse;
import com.petbook.petbook_backend.exceptions.rest.ChatAccessDeniedException;
import com.petbook.petbook_backend.exceptions.rest.ConversationNotFoundException;
import com.petbook.petbook_backend.exceptions.rest.UnauthorizedUserException;
import com.petbook.petbook_backend.exceptions.rest.UserNotFoundException;
import com.petbook.petbook_backend.models.Conversation;
import com.petbook.petbook_backend.models.Message;
import com.petbook.petbook_backend.models.Pet;
import com.petbook.petbook_backend.models.User;
import com.petbook.petbook_backend.repository.ConversationRepository;
import com.petbook.petbook_backend.repository.MessageRepository;
import com.petbook.petbook_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserServiceImpl userService;
    private final UserRepository userRepository;

    public ChatMessageResponse sendMessage(ChatMessageRequest request, Long authenticatedUserId) {
        // Override senderId to ensure no impersonation
        request.setSenderId(authenticatedUserId);

        Conversation conversation = conversationRepository.findById(request.getConversationId())
                .orElseThrow(() -> new ConversationNotFoundException("Conversation not found"));

        validateParticipant(conversation, authenticatedUserId);

        Message message = Message.builder()
                .conversation(conversation)
                .sender(User.builder().id(authenticatedUserId).build())
                .receiver(User.builder().id(request.getReceiverId()).build())
                .content(request.getContent())
                .isRead(false)
                .sentAt(LocalDateTime.now())
                .build();

        Message saved = messageRepository.save(message);

        ChatMessageResponse response = new ChatMessageResponse(
                saved.getId(),
                saved.getSender().getId(),
                saved.getReceiver().getId(),
                saved.getContent(),
                saved.getIsRead(),
                saved.getSentAt()
        );

        messagingTemplate.convertAndSend("/topic/conversation/" + conversation.getId(), response);
        return response;
    }

    public ConversationResponse startConversation(ConversationRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(username).orElseThrow(() -> new UserNotFoundException("User not found"));
        Long userId = user.getId();
        if(!Objects.equals(userId, request.getUser1Id()) && (!Objects.equals(userId, request.getUser2Id())) ) throw new UnauthorizedUserException("You are not authorized to start this conversation");

        Conversation conversation = conversationRepository
                .findBetweenUsersAndPet(request.getUser1Id(), request.getUser2Id(), request.getPetId())
                .orElseGet(() -> conversationRepository.save(
                        Conversation.builder()
                                .user1(User.builder().id(request.getUser1Id()).build())
                                .user2(User.builder().id(request.getUser2Id()).build())
                                .pet(request.getPetId() != null ? Pet.builder().id(request.getPetId()).build() : null)
                                .createdAt(LocalDateTime.now())
                                .build()
                ));


        User user1 = userService.loadUserById(conversation.getUser1().getId());
        User user2 = userService.loadUserById(conversation.getUser2().getId());

        return ConversationResponse.builder()
                .id(conversation.getId())
                .user1Id(user1.getId())
                .user1Name(user1.getFirstname() + " " + user1.getLastname())
                .user2Id(user2.getId())
                .user2Name(user2.getFirstname() + " " + user2.getLastname())
                .petId(conversation.getPet() != null ? conversation.getPet().getId() : null)
                .petName(conversation.getPet() != null ? conversation.getPet().getName() : null)
                .createdAt(conversation.getCreatedAt())
                .build();
    }

    public List<Long> markAsRead(MarkReadRequest request, Long authenticatedUserId) {
        // Ignore client-provided userId and use authenticated one
        validateParticipant(
                conversationRepository.findById(request.getConversationId())
                        .orElseThrow(() -> new ConversationNotFoundException("Conversation not found")),
                authenticatedUserId
        );

        int updatedCount = messageRepository.markAsRead(request.getConversationId(), authenticatedUserId);
        if (updatedCount == 0) {
            return List.of();
        }
        return messageRepository.findReadMessages(request.getConversationId(), authenticatedUserId);
    }

    public List<MessageResponse> getMessages(Long conversationId, Long authenticatedUserId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ConversationNotFoundException("Conversation not found"));

        validateParticipant(conversation, authenticatedUserId);

        return messageRepository.findByConversationIdOrderBySentAtAsc(conversationId)
                .stream()
                .map(message -> MessageResponse.builder()
                        .id(message.getId())
                        .senderId(message.getSender().getId())
                        .senderName(message.getSender().getFirstname() + " " + message.getSender().getLastname())
                        .receiverId(message.getReceiver().getId())
                        .receiverName(message.getReceiver().getFirstname() + " " + message.getReceiver().getLastname())
                        .content(message.getContent())
                        .read(message.getIsRead())
                        .sentAt(message.getSentAt())
                        .build())
                .toList();
    }

    private void validateParticipant(Conversation conversation, Long userId) {
        Long user1Id = conversation.getUser1().getId();
        Long user2Id = conversation.getUser2().getId();

        if (!userId.equals(user1Id) && !userId.equals(user2Id)) {
            throw new ChatAccessDeniedException("You are not part of this conversation");
        }
    }
}
