package com.petbook.petbook_backend.service;

import com.petbook.petbook_backend.dto.request.ChatMessageRequest;
import com.petbook.petbook_backend.dto.request.ConversationRequest;
import com.petbook.petbook_backend.dto.request.MarkReadRequest;
import com.petbook.petbook_backend.dto.response.ChatMessageResponse;
import com.petbook.petbook_backend.dto.response.ConversationResponse;
import com.petbook.petbook_backend.dto.response.MarkReadResponse;
import com.petbook.petbook_backend.dto.response.MessageResponse;
import com.petbook.petbook_backend.dto.sockets.SocketEvent;
import com.petbook.petbook_backend.exceptions.rest.ChatAccessDeniedException;
import com.petbook.petbook_backend.exceptions.rest.ConversationAlreadyExistsException;
import com.petbook.petbook_backend.exceptions.rest.ConversationNotFoundException;
import com.petbook.petbook_backend.exceptions.rest.UnauthorizedUserException;
import com.petbook.petbook_backend.exceptions.rest.UserNotFoundException;
import com.petbook.petbook_backend.models.Conversation;
import com.petbook.petbook_backend.models.CustomUserDetails;
import com.petbook.petbook_backend.models.EventType;
import com.petbook.petbook_backend.models.Message;
import com.petbook.petbook_backend.models.NotificationType;
import com.petbook.petbook_backend.models.Pet;
import com.petbook.petbook_backend.models.User;
import com.petbook.petbook_backend.repository.ConversationRepository;
import com.petbook.petbook_backend.repository.MessageRepository;
import com.petbook.petbook_backend.repository.UserRepository;
import com.petbook.petbook_backend.service.events.MessageSentEvent;
import com.petbook.petbook_backend.service.events.NotificationEvent;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserServiceImpl userService;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void sendMessage(ChatMessageRequest request, Long authenticatedUserId) {
        log.debug("SendMessage request: convId={}, senderId={}, receiverId={}", request.getConversationId(), authenticatedUserId, request.getReceiverId());


        request.setSenderId(authenticatedUserId);

        Conversation conversation = conversationRepository.findById(request.getConversationId()).orElseThrow(() -> new ConversationNotFoundException("Conversation not found"));

        validateParticipant(conversation, authenticatedUserId);

        Message message = Message.builder().conversation(conversation).sender(entityManager.getReference(User.class, authenticatedUserId)).receiver(entityManager.getReference(User.class, request.getReceiverId())).content(request.getContent()).isRead(false).sentAt(LocalDateTime.now()).build();
        Message saved = messageRepository.save(message);

        // Build response DTO (ideally via mapper)
        ChatMessageResponse response = ChatMessageResponse.builder().id(saved.getId()).senderId(authenticatedUserId).receiverId(request.getReceiverId()).senderName(saved.getSender().getFirstname() + " " + saved.getSender().getLastname()).receiverName(saved.getReceiver().getFirstname() + " " + saved.getReceiver().getLastname()).content(saved.getContent()).read(message.getIsRead()).sentAt(saved.getSentAt()).build();


        applicationEventPublisher.publishEvent(MessageSentEvent.builder()
                .recipientId(request.getReceiverId())
                .conversationId(request.getConversationId())
                .build());
        messagingTemplate.convertAndSend("/topic/conversation/" + conversation.getId(), new SocketEvent<>(EventType.MESSAGE_SENT, response));


    }

    @Transactional
    public void markAsRead(MarkReadRequest request, Long authenticatedUserId) {
        // Ignore client-provided userId and use authenticated one
        validateParticipant(conversationRepository.findById(request.getConversationId()).orElseThrow(() -> new ConversationNotFoundException("Conversation not found")), authenticatedUserId);

        int updatedCount = messageRepository.markAsRead(request.getConversationId(), authenticatedUserId);
        if (updatedCount == 0) {
            return;
        }
        List<Long> readReceipts = messageRepository.findReadMessages(request.getConversationId(), authenticatedUserId);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                messagingTemplate.convertAndSend("/topic/conversation/" + request.getConversationId(), new SocketEvent<>(EventType.MESSAGE_READ, MarkReadResponse.builder().readMessageIds(readReceipts).build()));
            }
        });

    }

    @Transactional
    public ConversationResponse startConversation(ConversationRequest request) {
        User authenticatedUser = getAuthenticatedUser();
        Long authenticatedUserId = authenticatedUser.getId();
        if (!Objects.equals(authenticatedUserId, request.getUser1Id()) && (!Objects.equals(authenticatedUserId, request.getUser2Id())))
            throw new UnauthorizedUserException("You are not authorized to start this conversation");

        Optional<Conversation> existingConversation = conversationRepository.findBetweenUsersAndPet(request.getUser1Id(), request.getUser2Id(), request.getPetId());
        Conversation newConversation;
        User user1 = userService.loadUserById(request.getUser1Id());
        User user2 = userService.loadUserById(request.getUser2Id());
        Long user1id = user1.getId();
        Long user2id = user2.getId();


        //The recipient of this notification *NOT YOU*
        String recipientEmail = (authenticatedUserId.equals(user1id) ? user2.getEmail() : user1.getEmail());
        Long recipientId = (authenticatedUserId.equals(user1id) ? user2id : user1id);
        String user1Name = user1.getFirstname() + " " + user1.getLastname();
        String user2Name = user2.getFirstname() + " " + user2.getLastname();

        //The sender *YOU*
        String senderName = authenticatedUserId.equals(user1id) ? user1Name : user2Name;


        if (existingConversation.isPresent()) {
            throw new ConversationAlreadyExistsException("You are already in a conversation with the owner of this pet listing");
        } else {

            applicationEventPublisher.publishEvent(NotificationEvent.builder()
                    .recipientUserId(recipientId)
                    .recipientEmail(recipientEmail)
                    .message(senderName + " started a conversation with you")
                    .type(NotificationType.CONVERSATION_STARTED)
                    .build());


            newConversation = conversationRepository.save(Conversation.builder()
                    .user1(user1)
                    .user2(user2)
                    .pet(request.getPetId() != null ? Pet.builder().id(request.getPetId()).build() : null)
                    .createdAt(LocalDateTime.now())
                    .build());
        }


        return ConversationResponse.builder()
                .id(newConversation.getId())
                .user1Id(user1id).user1Name(user1Name)
                .user2Id(user2id).user2Name(user2Name)
                .petId(newConversation.getPet() != null ? newConversation.getPet().getId() : null)
                .petName(newConversation.getPet() != null ? newConversation.getPet().getName() : null)
                .createdAt(newConversation.getCreatedAt())
                .build();
    }

    @Transactional
    public List<MessageResponse> getMessages(Long conversationId, Long authenticatedUserId) {
        Conversation conversation = conversationRepository.findById(conversationId).orElseThrow(() -> new ConversationNotFoundException("Conversation not found"));

        validateParticipant(conversation, authenticatedUserId);
        return messageRepository.findMessagesByConversationId(conversationId);
    }

    @Transactional
    public ConversationResponse deleteConversation(Long conversationId) {
        User authenticatedUser = getAuthenticatedUser();
        Long authenticatedUserId = authenticatedUser.getId();

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("No conversation found"));

        Long user1id = conversation.getUser1().getId();
        Long user2id = conversation.getUser2().getId();

        if (!Objects.equals(user1id, authenticatedUserId) && !Objects.equals(user2id, authenticatedUserId)) {
            throw new UnauthorizedUserException("You are not authorized to delete this conversation");
        }

        String user1Name = conversation.getUser1().getFirstname() + " " + conversation.getUser1().getLastname();
        String user2Name = conversation.getUser2().getFirstname() + " " + conversation.getUser2().getLastname();

        // ✅ Recipient should be the *other* user
        String recipientEmail = (authenticatedUserId.equals(user1id) ? conversation.getUser2().getEmail() : conversation.getUser1().getEmail());
        Long recipientId = (authenticatedUserId.equals(user1id) ? user2id : user1id);

        // ✅ Sender is always the authenticated user
        String senderName = authenticatedUserId.equals(user1id) ? user1Name : user2Name;

        conversationRepository.delete(conversation);

        applicationEventPublisher.publishEvent(NotificationEvent.builder()
                .recipientUserId(recipientId)
                .recipientEmail(recipientEmail)
                .message(senderName + " ended a conversation with you")
                .type(NotificationType.CONVERSATION_DELETED)
                .build());

        return ConversationResponse.builder()
                .id(conversation.getId())
                .user1Id(user1id).user1Name(user1Name)
                .user2Id(user2id).user2Name(user2Name)
                .petId(conversation.getPet() != null ? conversation.getPet().getId() : null)
                .petName(conversation.getPet() != null ? conversation.getPet().getName() : null)
                .createdAt(conversation.getCreatedAt())
                .build();
    }

    private User getAuthenticatedUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    @Transactional(readOnly = true)
    public List<ConversationResponse> getUserConversations(CustomUserDetails userDetails) {
        Long requestingUserId = userDetails.getId();
        return conversationRepository.findByUserId(requestingUserId);
    }

    private void validateParticipant(Conversation conversation, Long userId) {
        Long user1Id = conversation.getUser1().getId();
        Long user2Id = conversation.getUser2().getId();

        if (!userId.equals(user1Id) && !userId.equals(user2Id)) {
            throw new ChatAccessDeniedException("You are not part of this conversation");
        }
    }

    @Transactional
    public ConversationResponse getConversation(Long conversationId, CustomUserDetails userDetails) {
//        String username = userDetails.getUsername();
//        User user = userRepository.findByEmail(username).orElseThrow(() -> new UserNotFoundException("User not found"));
        return conversationRepository.findConversationById(conversationId).orElseThrow(() -> new RuntimeException("No such conversation found"));

//        Conversation conversation = conversationRepository.findById(conversationId).orElseThrow(() -> new RuntimeException("No such conversation found"));
//        validateParticipant(conversation, user.getId());
//        return ConversationResponse.builder().id(conversation.getId()).user1Id(conversation.getUser1().getId()).user1Name(conversation.getUser1().getFirstname() + " " + conversation.getUser1().getLastname()).user2Id(conversation.getUser2().getId()).user2Name(conversation.getUser2().getFirstname() + " " + conversation.getUser2().getLastname()).petId(conversation.getPet() != null ? conversation.getPet().getId() : null).petName(conversation.getPet() != null ? conversation.getPet().getName() : null).createdAt(conversation.getCreatedAt()).build();

    }

}
