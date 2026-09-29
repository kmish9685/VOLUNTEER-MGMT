package com.volunteer.platform.repository;

import com.volunteer.platform.model.Message;
import com.volunteer.platform.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * =====================================================================
 * MessageRepository (Data Access Layer)
 * ---------------------------------------------------------------------
 * Handles database operations for direct messages between users.
 * Supports chronological chat retrieval, unread counters, and mark-as-read
 * workflows.
 * =====================================================================
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    /**
     * Retrieves the complete chronological chat history between two users.
     */
    @Query("SELECT m FROM Message m WHERE (m.sender = :userA AND m.receiver = :userB) " +
           "OR (m.sender = :userB AND m.receiver = :userA) ORDER BY m.sentAt ASC")
    List<Message> findConversation(@Param("userA") User userA, @Param("userB") User userB);

    /**
     * Counts the total number of unread incoming messages for a recipient.
     */
    long countByReceiverAndIsReadFalse(User receiver);

    /**
     * Retrieves all unread messages sent by a specific user to the recipient.
     */
    List<Message> findBySenderAndReceiverAndIsReadFalse(User sender, User receiver);
}
