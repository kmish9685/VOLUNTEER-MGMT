package com.volunteer.platform.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * =====================================================================
 * Registration Entity (Model Layer)
 * ---------------------------------------------------------------------
 * Represents a volunteer's enrollment in a specific opportunity.
 * Enforces a unique constraint so a volunteer cannot sign up for the
 * same event multiple times.
 *
 * Stored in the "registrations" table in PostgreSQL (Supabase).
 * =====================================================================
 */
@Entity
@Table(name = "registrations", uniqueConstraints = {
    @UniqueConstraint(name = "uk_volunteer_opportunity", columnNames = {"volunteer_id", "opportunity_id"})
})
public class Registration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "volunteer_id", nullable = false)
    private User volunteer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "opportunity_id", nullable = false)
    private Opportunity opportunity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RegistrationStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime registeredAt;

    /**
     * Default no-argument constructor required by JPA/Hibernate.
     */
    public Registration() {
    }

    /**
     * Parameterized constructor for enrolling a volunteer into an opportunity.
     */
    public Registration(User volunteer, Opportunity opportunity, RegistrationStatus status) {
        this.volunteer = volunteer;
        this.opportunity = opportunity;
        this.status = status;
        this.registeredAt = LocalDateTime.now();
    }

    /**
     * Automatically sets registeredAt timestamp before persisting to database.
     */
    @PrePersist
    protected void onCreate() {
        if (this.registeredAt == null) {
            this.registeredAt = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = RegistrationStatus.REGISTERED;
        }
    }

    // ==========================================
    // GETTERS AND SETTERS (Explicit, No Lombok)
    // ==========================================

    /**
     * Returns the unique database ID of the registration.
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the database ID of the registration.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Returns the volunteer user enrolled in this registration.
     */
    public User getVolunteer() {
        return volunteer;
    }

    /**
     * Sets the volunteer user enrolled in this registration.
     */
    public void setVolunteer(User volunteer) {
        this.volunteer = volunteer;
    }

    /**
     * Returns the opportunity event for which the volunteer registered.
     */
    public Opportunity getOpportunity() {
        return opportunity;
    }

    /**
     * Sets the opportunity event for which the volunteer registered.
     */
    public void setOpportunity(Opportunity opportunity) {
        this.opportunity = opportunity;
    }

    /**
     * Returns the attendance/participation status (REGISTERED, ATTENDED, ABSENT, CANCELLED).
     */
    public RegistrationStatus getStatus() {
        return status;
    }

    /**
     * Sets the attendance/participation status.
     */
    public void setStatus(RegistrationStatus status) {
        this.status = status;
    }

    /**
     * Returns the exact timestamp when the volunteer registered.
     */
    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    /**
     * Sets the timestamp when the volunteer registered.
     */
    public void setRegisteredAt(LocalDateTime registeredAt) {
        this.registeredAt = registeredAt;
    }
}
