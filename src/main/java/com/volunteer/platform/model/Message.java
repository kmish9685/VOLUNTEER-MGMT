package com.volunteer.platform.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * =====================================================================
 * Message Entity (Model Layer)
 * ---------------------------------------------------------------------
 * Represents a direct 1-on-1 message exchanged between an Organization
 * and a Volunteer who registered for one of their opportunities.
 *
 * Stored in the "messages" table in MySQL.
 * =====================================================================
 */
@Entity
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false, updatable = false)
    private LocalDateTime sentAt;

    @Column(nullable = false)
    private boolean isRead;

    /**
     * Default no-argument constructor required by JPA/Hibernate.
     */
    public Message() {
    }

    /**
     * Parameterized constructor for sending a new message.
     */
    public Message(User sender, User receiver, String content) {
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.sentAt = LocalDateTime.now();
        this.isRead = false;
    }

    /**
     * Automatically sets sentAt timestamp before persisting to database.
     */
    @PrePersist
    protected void onCreate() {
        if (this.sentAt == null) {
            this.sentAt = LocalDateTime.now();
        }
    }

    // ==========================================
    // GETTERS AND SETTERS (Explicit, No Lombok)
    // ==========================================

    /**
     * Returns the unique database ID of the message.
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the database ID of the message.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Returns the sender user account.
     */
    public User getSender() {
        return sender;
    }

    /**
     * Sets the sender user account.
     */
    public void setSender(User sender) {
        this.sender = sender;
    }

    /**
     * Returns the receiver user account.
     */
    public User getReceiver() {
        return receiver;
    }

    /**
     * Sets the receiver user account.
     */
    public void setReceiver(User receiver) {
        this.receiver = receiver;
    }

    /**
     * Returns the body text of the message.
     */
    public String getContent() {
        return content;
    }

    /**
     * Sets the body text of the message.
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Returns the timestamp when the message was sent.
     */
    public LocalDateTime getSentAt() {
        return sentAt;
    }

    /**
     * Sets the timestamp when the message was sent.
     */
    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    /**
     * Returns whether the message has been read by the recipient.
     */
    public boolean isRead() {
        return isRead;
    }

    /**
     * Sets the read status of the message.
     */
    public void setRead(boolean read) {
        isRead = read;
    }
}
