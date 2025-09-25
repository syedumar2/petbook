package com.petbook.petbook_backend.repository;

import com.petbook.petbook_backend.dto.response.MessageResponse;
import com.petbook.petbook_backend.models.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    @Modifying
    @Transactional
    @Query("UPDATE Message m " +
            "SET m.isRead = true " +
            "WHERE m.conversation.id = :conversationId "
            + "AND m.receiver.id = :receiverId "
            + "AND m.isRead = false")
    int markAsRead(@Param("conversationId") Long conversationId,
                   @Param("receiverId") Long receiverId);

    @Query(
            "SELECT m.id FROM Message m "
                    + "WHERE m.conversation.id = :conversationId "
                    + "AND m.receiver.id = :userId "
                    + "AND m.isRead = true "
                    + "ORDER BY m.sentAt ASC"
    )
    List<Long> findReadMessages(Long conversationId, Long userId);

    @Query("SELECT COUNT(m) FROM Message m " +
            "WHERE (m.sender.id = :userId OR m.receiver.id = :userId) "
            + "AND m.isRead = false")
    Long findUnreadMessagesCount(Long userId);

    @Query("SELECT COUNT(DISTINCT m.conversation.id) FROM Message m "
            + "WHERE (m.sender.id = :userId OR m.receiver.id = :userId) "
            + "AND m.isRead = false")
    Long countDistinctConversationsWithUnreadMessages(Long userId);

    @Query("SELECT m FROM Message m JOIN FETCH m.sender JOIN FETCH m.receiver WHERE m.conversation.id = :conversationId")
    List<Message> findByConversationIdOrderBySentAtAsc(Long conversationId);

    @Query("""
             SELECT new com.petbook.petbook_backend.dto.response.MessageResponse(
             m.id,
            u1.id,  CONCAT(u1.firstname, ' ', u1.lastname),
            u2.id,  CONCAT(u2.firstname, ' ', u2.lastname),
            m.content,
            m.isRead,
            m.sentAt
             ) FROM Message m
              JOIN m.sender u1
            JOIN m.receiver u2
            WHERE m.conversation.id = :conversationId
            ORDER BY m.sentAt ASC
            """)
    List<MessageResponse> findMessagesByConversationId(Long conversationId);
}
