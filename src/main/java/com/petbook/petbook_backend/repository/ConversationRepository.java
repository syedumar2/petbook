package com.petbook.petbook_backend.repository;

import com.petbook.petbook_backend.models.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}
