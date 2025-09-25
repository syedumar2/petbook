package com.petbook.petbook_backend.repository;

import com.petbook.petbook_backend.dto.response.ConversationResponse;
import com.petbook.petbook_backend.models.Conversation;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {


    @Query("""
            SELECT c FROM Conversation c
            WHERE ((c.user1.id = :user1Id AND c.user2.id = :user2Id) OR (c.user1.id = :user2Id AND c.user2.id = :user1Id))
            AND (:petId IS NULL OR c.pet.id = :petId)""")
    Optional<Conversation> findBetweenUsersAndPet(@Param("user1Id") Long user1Id,
                                                  @Param("user2Id") Long user2Id,
                                                  @Param("petId") Long petId);

    @Query("""
            SELECT new com.petbook.petbook_backend.dto.response.ConversationResponse(
                c.id,
                u1.id,  CONCAT(u1.firstname, ' ', u1.lastname),
                u2.id,  CONCAT(u2.firstname, ' ', u2.lastname),
                p.id, p.name,
                c.createdAt
            ) FROM Conversation c
            JOIN c.user1 u1
            JOIN c.user2 u2
            LEFT JOIN c.pet p
            WHERE c.user1.id = :userId OR c.user2.id = :userId
            """)
    List<ConversationResponse> findByUserId(Long userId);


    @Query("""
            SELECT new com.petbook.petbook_backend.dto.response.ConversationResponse(
                c.id,
                u1.id,  CONCAT(u1.firstname, ' ', u1.lastname),
                u2.id,  CONCAT(u2.firstname, ' ', u2.lastname),
                p.id, p.name,
                c.createdAt
            ) FROM Conversation c
            JOIN c.user1 u1
            JOIN c.user2 u2
            LEFT JOIN c.pet p
            WHERE c.id = :conversationId
            """)
    Optional<ConversationResponse> findConversationById(@Param("conversationId") Long conversationId);

}
