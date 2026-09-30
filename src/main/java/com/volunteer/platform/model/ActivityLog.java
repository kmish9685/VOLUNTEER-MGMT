package com.volunteer.platform.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * =====================================================================
 * ActivityLog Entity (Model Layer)
 * ---------------------------------------------------------------------
 * Records system actions and audit events across the platform.
 * Allows the Admin to monitor registrations, logins, opportunity updates,
 * attendance changes, and configuration edits.
 *
 * Stored in the "activity_logs" table in PostgreSQL (Supabase).
 * =====================================================================
 */
@Entity
@Table(name = "activity_logs")
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    @Column(nullable = false, length = 255)
    private String action;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Default no-argument constructor required by JPA/Hibernate.
     */
    public ActivityLog() {
    }

    /**
     * Parameterized constructor for recording a new activity log entry.
     */
    public ActivityLog(User user, String action) {
        this.user = user;
        this.action = action;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Automatically sets createdAt timestamp before persisting to database.
     */
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    // ==========================================
    // GETTERS AND SETTERS (Explicit, No Lombok)
    // ==========================================

    /**
     * Returns the unique database ID of the activity log entry.
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the database ID of the activity log entry.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Returns the user who triggered this action (can be null for guest/system events).
     */
    public User getUser() {
        return user;
    }

    /**
     * Sets the user who triggered this action.
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Returns the descriptive text of the action performed.
     */
    public String getAction() {
        return action;
    }

    /**
     * Sets the descriptive text of the action performed.
     */
    public void setAction(String action) {
        this.action = action;
    }

    /**
     * Returns the timestamp when the activity occurred.
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the timestamp when the activity occurred.
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
