package com.petbook.petbook_backend.controller;

import com.petbook.petbook_backend.dto.request.NotificationsDeleteRequest;
import com.petbook.petbook_backend.dto.response.ApiResponse;
import com.petbook.petbook_backend.dto.sockets.NotificationPayload;
import com.petbook.petbook_backend.models.CustomUserDetails;
import com.petbook.petbook_backend.service.NotificationService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public ResponseEntity<ApiResponse<List<NotificationPayload>>> getAllNotifications(@AuthenticationPrincipal CustomUserDetails userDetails) {
        List<NotificationPayload> notificationsList = notificationService.getAllNotifications(userDetails);
        return ResponseEntity.ok(ApiResponse.successWithCount(notificationsList.size(), "Notification retrieved successfully", notificationsList));
    }

    @GetMapping("auth/notifications")
    public ResponseEntity<ApiResponse<List<NotificationPayload>>> getUnreadNotifications(@AuthenticationPrincipal CustomUserDetails userDetails) {
        List<NotificationPayload> notificationsList = notificationService.getUnreadNotifications(userDetails);
        return ResponseEntity.ok(ApiResponse.successWithCount(notificationsList.size(), "Notification retrieved successfully", notificationsList));
    }


    @GetMapping("auth/notifications/{notificationId}")
    public ResponseEntity<ApiResponse<NotificationPayload>> markAsReadNotification(@PathVariable @NotNull Long notificationId, @AuthenticationPrincipal CustomUserDetails userDetails) {
        NotificationPayload notification = notificationService.markRead(notificationId, userDetails);
        return ResponseEntity.ok(ApiResponse.success("Notification marked", notification));
    }


    @DeleteMapping("auth/notifications")
    public ResponseEntity<ApiResponse<String>> deleteBulkNotifications(@RequestBody @NotNull NotificationsDeleteRequest request, @AuthenticationPrincipal CustomUserDetails userDetails) {
        String notificationMsg = notificationService.deleteBulkNotifications(request, userDetails);
        return ResponseEntity.ok(ApiResponse.successWithMessage(notificationMsg));
    }


}
