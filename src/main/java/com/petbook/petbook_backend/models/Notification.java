package com.petbook.petbook_backend.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(nullable = false)
    private Long recipientId;

    @Column(nullable = false)
    private String message;
    @Column(name = "notification_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationType type;

    private boolean isRead = false;

    private LocalDateTime createdAt = LocalDateTime.now();


    public static Notification petApproved(Long recipientId, String message) {
        return Notification.builder()
                .recipientId(recipientId)
                .type(NotificationType.PET_APPROVED)
                .isRead(false)
                .message(message)
                .build();
    }
}
