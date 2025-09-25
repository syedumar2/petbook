package com.petbook.petbook_backend.service;

import com.petbook.petbook_backend.dto.request.NotificationsDeleteRequest;
import com.petbook.petbook_backend.dto.sockets.NotificationPayload;
import com.petbook.petbook_backend.exceptions.rest.UnauthorizedUserException;
import com.petbook.petbook_backend.models.CustomUserDetails;
import com.petbook.petbook_backend.models.Notification;
import com.petbook.petbook_backend.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;

    //====================== READ SERVICES ======================

    @Transactional(readOnly = true)
    public List<NotificationPayload> getAllNotifications(CustomUserDetails userDetails) {
        Long userId = userDetails.getId();

        return notificationRepository.findByRecipientId(userId)
                .stream()
                .map(this::mapToPayload)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<NotificationPayload> getUnreadNotifications(CustomUserDetails userDetails) {
        Long userId = userDetails.getId();

        return notificationRepository.findByRecipientIdAndIsReadFalse(userId)
                .stream()
                .map(this::mapToPayload)
                .collect(Collectors.toList());
    }

    @Transactional
    public NotificationPayload markRead(Long notificationId, CustomUserDetails userDetails) {
        Long userId = userDetails.getId();

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("No such notification found"));

        if (!userId.equals(notification.getRecipientId())) {
            throw new UnauthorizedUserException("You are not authorized to mark this notification as read");
        }

        notification.setRead(true);

        return mapToPayload(notification);
    }

    //====================== DELETE SERVICES ======================

    @Transactional
    public String deleteSingleNotification(@NotNull Long notificationId, CustomUserDetails userDetails) {
        Long userId = userDetails.getId();

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("No such notification found"));

        if (!userId.equals(notification.getRecipientId())) {
            throw new UnauthorizedUserException("You are not authorized to delete this notification");
        }

        notificationRepository.deleteById(notificationId);
        return "Notification id:" + notificationId + " deleted";
    }

    @Transactional
    public String deleteBulkNotifications(NotificationsDeleteRequest request, CustomUserDetails userDetails) {
        Long userId = userDetails.getId();

        List<Notification> notificationList = notificationRepository.findAllById(request.getIds());

        boolean belongsToUser = notificationList.stream().allMatch(n -> n.getRecipientId().equals(userId));
        if (!belongsToUser) {
            throw new RuntimeException("You are not authorized to delete these notifications");
        }

        List<Long> deletedIds = notificationList.stream().map(Notification::getId).toList();
        notificationRepository.deleteAll(notificationList);
        return "Deleted notifications with IDs: " + deletedIds;
    }

    //====================== HELPERS ======================

    private NotificationPayload mapToPayload(Notification notification) {
        return NotificationPayload.builder()
                .id(notification.getId())
                .type(notification.getType().toString())
                .message(notification.getMessage())
                .read(notification.isRead())
                .createdAt(notification.getCreatedAt().toString())
                .build();
    }
}
