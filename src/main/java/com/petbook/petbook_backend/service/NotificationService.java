package com.petbook.petbook_backend.service;

import com.petbook.petbook_backend.dto.request.NotificationsDeleteRequest;
import com.petbook.petbook_backend.dto.sockets.NotificationPayload;
import com.petbook.petbook_backend.exceptions.rest.UnauthorizedUserException;
import com.petbook.petbook_backend.exceptions.rest.UserNotFoundException;
import com.petbook.petbook_backend.models.Notification;
import com.petbook.petbook_backend.models.User;
import com.petbook.petbook_backend.repository.NotificationRepository;
import com.petbook.petbook_backend.repository.UserRepository;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<NotificationPayload> getAllNotifications() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(username).orElseThrow(() -> new UserNotFoundException("User not found"));
        Long userId = user.getId();
        return notificationRepository.findByRecipientId(userId).stream().map(
                notification -> NotificationPayload.builder()
                        .id(notification.getId())
                        .type(notification.getType().toString())
                        .message(notification.getMessage())
                        .read(notification.isRead())
                        .createdAt(notification.getCreatedAt().toString())
                        .build()


        ).collect(Collectors.toList());


    }

    @Transactional(readOnly = true)
    public List<NotificationPayload> getUnreadNotifications() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(username).orElseThrow(() -> new UserNotFoundException("User not found"));
        Long userId = user.getId();
        return notificationRepository.findByRecipientIdAndIsReadFalse(userId).stream().map(
                notification -> NotificationPayload.builder()
                        .id(notification.getId())
                        .type(notification.getType().toString())
                        .message(notification.getMessage())
                        .read(notification.isRead())
                        .createdAt(notification.getCreatedAt().toString())
                        .build()


        ).collect(Collectors.toList());

    }

    @Transactional
    public NotificationPayload markRead(Long notificationId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(username).orElseThrow(() -> new UserNotFoundException("User not found"));
        Long userId = user.getId();
        Notification notification = notificationRepository.findById(notificationId).orElseThrow(() -> new RuntimeException("No such notification found"));
        if (!userId.equals(notification.getRecipientId())) {
            throw new UnauthorizedUserException("You are not authorized to mark this notification as read");
        }
        notification.setRead(true);

        return NotificationPayload.builder()
                .id(notification.getId())
                .message(notification.getMessage())
                .read(notification.isRead())
                .type(notification.getType().toString())
                .build();
    }

    @Transactional
    public String deleteSingleNotification(@NotNull Long notificationId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(username).orElseThrow(() -> new UserNotFoundException("User not found"));
        Long userId = user.getId();
        Notification notification = notificationRepository.findById(notificationId).orElseThrow(() -> new RuntimeException("No such notification found"));
        if (!userId.equals(notification.getRecipientId())) {
            throw new UnauthorizedUserException("You are not authorized to mark this notification as read");
        }
        notificationRepository.deleteById(notificationId);
        return "Notification id:" + notificationId + " deleted";


    }

    public String deleteBulkNotifications(NotificationsDeleteRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(username).orElseThrow(() -> new UserNotFoundException("User not found"));
        Long userId = user.getId();
        List<Notification> notificationList = notificationRepository.findAllById(request.getIds());
        boolean belongsToUser = notificationList.stream().allMatch(n ->
                n.getRecipientId().equals(userId));
        if (!belongsToUser) {
            throw new RuntimeException("You are not authorized to delete this notification");
        }
        List<Long> deletedIds = notificationList.stream().map(Notification::getId).toList();
        notificationRepository.deleteAll(notificationList);
        return "Deleted notifications with IDs: " + deletedIds;


    }


}
