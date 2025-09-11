package com.petbook.petbook_backend.controller;

import com.petbook.petbook_backend.dto.request.NotificationsDeleteRequest;
import com.petbook.petbook_backend.dto.response.ApiResponse;
import com.petbook.petbook_backend.dto.sockets.NotificationPayload;
import com.petbook.petbook_backend.service.NotificationService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class NotificationsController {
    private final NotificationService notificationService;


    @GetMapping("auth/notifications/all")
    public ResponseEntity<ApiResponse<List<NotificationPayload>>> getAllNotifications() {
        List<NotificationPayload> notificationsList = notificationService.getAllNotifications();
        return ResponseEntity.ok(ApiResponse.successWithCount(notificationsList.size(), "Notification retrieved successfully", notificationsList));
    }

    @GetMapping("auth/notifications")
    public ResponseEntity<ApiResponse<List<NotificationPayload>>> getUnreadNotifications() {
        List<NotificationPayload> notificationsList = notificationService.getUnreadNotifications();
        return ResponseEntity.ok(ApiResponse.successWithCount(notificationsList.size(), "Notification retrieved successfully", notificationsList));
    }


    @GetMapping("auth/notifications/{notificationId}")
    public ResponseEntity<ApiResponse<NotificationPayload>> markAsReadNotification(@PathVariable @NotNull Long notificationId) {
        NotificationPayload notification = notificationService.markRead(notificationId);
        return ResponseEntity.ok(ApiResponse.success("Notification marked", notification));
    }



    @DeleteMapping("auth/notifications")
    public ResponseEntity<ApiResponse<String>> deleteBulkNotifications(@RequestBody @NotNull NotificationsDeleteRequest request) {
        String notificationMsg = notificationService.deleteBulkNotifications(request);
        return ResponseEntity.ok(ApiResponse.successWithMessage(notificationMsg));
    }



}
