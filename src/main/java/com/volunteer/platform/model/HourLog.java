package com.volunteer.platform.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * =====================================================================
 * HourLog Entity (Model Layer)
 * ---------------------------------------------------------------------
 * Records the service hours contributed by a volunteer who attended an event.
 * Hours are counted immediately when submitted — no approval step required.
 *
 * Stored in the "hour_logs" table in PostgreSQL (Supabase).
 * =====================================================================
 */
@Entity
@Table(name = "hour_logs")
public class HourLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registration_id", nullable = false)
    private Registration registration;

    @Column(nullable = false)
    private double hours;

    @Column(columnDefinition = "TEXT")
    private String workDescription;

    @Column(nullable = false, updatable = false)
    private LocalDateTime loggedAt;

    /**
     * Default no-argument constructor required by JPA/Hibernate.
     */
    public HourLog() {
    }

    /**
     * Parameterized constructor for creating a new hour log record.
     */
    public HourLog(Registration registration, double hours, String workDescription) {
        this.registration = registration;
        this.hours = hours;
        this.workDescription = workDescription;
        this.loggedAt = LocalDateTime.now();
    }

    /**
     * Automatically sets loggedAt timestamp before persisting to database.
     */
    @PrePersist
    protected void onCreate() {
        if (this.loggedAt == null) {
            this.loggedAt = LocalDateTime.now();
        }
    }

    // ==========================================
    // GETTERS AND SETTERS (Explicit, No Lombok)
    // ==========================================

    /**
     * Returns the unique database ID of this hour log entry.
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the database ID of this hour log entry.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Returns the associated registration record.
     */
    public Registration getRegistration() {
        return registration;
    }

    /**
     * Sets the associated registration record.
     */
    public void setRegistration(Registration registration) {
        this.registration = registration;
    }

    /**
     * Returns the number of volunteer hours logged.
     */
    public double getHours() {
        return hours;
    }

    /**
     * Sets the number of volunteer hours logged.
     */
    public void setHours(double hours) {
        this.hours = hours;
    }

    /**
     * Returns the summary description of the work completed during the event.
     */
    public String getWorkDescription() {
        return workDescription;
    }

    /**
     * Sets the summary description of the work completed during the event.
     */
    public void setWorkDescription(String workDescription) {
        this.workDescription = workDescription;
    }

    /**
     * Returns the timestamp when the hours were logged.
     */
    public LocalDateTime getLoggedAt() {
        return loggedAt;
    }

    /**
     * Sets the timestamp when the hours were logged.
     */
    public void setLoggedAt(LocalDateTime loggedAt) {
        this.loggedAt = loggedAt;
    }
}
