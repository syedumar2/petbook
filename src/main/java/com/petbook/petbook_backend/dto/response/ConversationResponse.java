package com.petbook.petbook_backend.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConversationResponse {
    private Long id;
    private Long user1Id;
    private String user1Name;
    private Long user2Id;
    private String user2Name;
    private Long petId;
    private String petName;
    private LocalDateTime createdAt;
}
