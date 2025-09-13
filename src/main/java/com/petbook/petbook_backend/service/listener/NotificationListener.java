package com.petbook.petbook_backend.service.listener;

import com.petbook.petbook_backend.dto.sockets.NotificationPayload;
import com.petbook.petbook_backend.dto.sockets.SocketEvent;
import com.petbook.petbook_backend.models.EventType;
import com.petbook.petbook_backend.models.Notification;
import com.petbook.petbook_backend.models.NotificationType;
import com.petbook.petbook_backend.repository.MessageRepository;
import com.petbook.petbook_backend.repository.NotificationRepository;
import com.petbook.petbook_backend.service.UserSessionService;
import com.petbook.petbook_backend.service.events.MessageSentEvent;
import com.petbook.petbook_backend.service.events.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationListener {
    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserSessionService userSessionService;
    private final MessageRepository messageRepository;


    @EventListener
    public void handleNotificationEvent(NotificationEvent notificationEvent) {
        Notification notification = new Notification();
        notification.setRecipientId(notificationEvent.getRecipientUserId());
        notification.setMessage(notificationEvent.getMessage());
        notification.setType(notificationEvent.getType());

        Notification savedNotification = notificationRepository.save(notification);


        messagingTemplate.convertAndSendToUser(notificationEvent.getRecipientEmail(), "/queue/notifications", new SocketEvent<>(EventType.NOTIFICATION, NotificationPayload.builder()
                .id(savedNotification.getId())
                .type(String.valueOf(savedNotification.getType()))
                .read(savedNotification.isRead())
                .message(savedNotification.getMessage())
                .createdAt(String.valueOf(savedNotification.getCreatedAt()))
                .build()

        ));
    }

    @EventListener
    public void onMessageSent(MessageSentEvent event) {
        log.info("Received MessageSentEvent: recipientId={}, conversationId={}",
                event.getRecipientId(), event.getConversationId());

        Long recipientId = event.getRecipientId();
        Long conversationId = event.getConversationId();

        long totalUnread = messageRepository.findUnreadMessagesCount(recipientId);
        long conversationsUnread = messageRepository.countDistinctConversationsWithUnreadMessages(recipientId);

        Notification notification = new Notification();
        notification.setRecipientId(recipientId);
        notification.setMessage("You have " + totalUnread + " new messages from " + conversationsUnread + " different conversations");
        notification.setType(NotificationType.NEW_MESSAGES);
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());

        //if the user is online but not subscribed to a conversation yet, then we need to send them a notification that they are getting messages
        if (userSessionService.isUserOnline(recipientId) &&
                !userSessionService.isUserInConversation(recipientId, conversationId)) {
            try {
                Notification savedNotification = notificationRepository.saveAndFlush(notification);
                log.info("Notification saved successfully: id={}", savedNotification.getId());

                messagingTemplate.convertAndSendToUser(
                        String.valueOf(recipientId),
                        "/queue/notifications",
                        new SocketEvent<>(
                                EventType.NOTIFICATION,
                                NotificationPayload.builder()
                                        .id(savedNotification.getId())
                                        .type(String.valueOf(savedNotification.getType()))
                                        .read(savedNotification.isRead())
                                        .message(savedNotification.getMessage())
                                        .createdAt(String.valueOf(savedNotification.getCreatedAt()))
                                        .build()
                        )
                );
            } catch (Exception e) {
                log.error("Failed to save notification: {}", e.getMessage(), e);
                throw e; // Re-throw for debugging
            }
        }
        //in case the user is offline this whats gonna happen, the notification will be saved to the db so it can be fetched later
        else if (!userSessionService.isUserOnline(recipientId) && !userSessionService.isUserInConversation(recipientId, conversationId)) {
            try {
                Notification savedNotification = notificationRepository.saveAndFlush(notification);
                log.info("Notification saved successfully when user is offline: id={}", savedNotification.getId());

            } catch (Exception e) {
                log.error("Failed to save notification: {}", e.getMessage(), e);
                throw e; // Re-throw for debugging
            }

        } else {
            log.info("Notification skipped: user online={}, in conversation={}",
                    userSessionService.isUserOnline(recipientId),
                    userSessionService.isUserInConversation(recipientId, conversationId));
        }
    }


}
