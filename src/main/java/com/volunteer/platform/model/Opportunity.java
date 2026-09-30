package com.volunteer.platform.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * =====================================================================
 * Opportunity Entity (Model Layer)
 * ---------------------------------------------------------------------
 * Represents a volunteering event or community initiative posted by an
 * Organization. It has a review lifecycle (PENDING -> APPROVED / REJECTED)
 * before volunteers can view and register for it.
 *
 * Stored in the "opportunities" table in PostgreSQL (Supabase).
 * =====================================================================
 */
@Entity
@Table(name = "opportunities")
public class Opportunity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 200)
    private String location;

    @Column(nullable = false)
    private LocalDate eventDate;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @Column(nullable = false)
    private int totalSlots;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OpportunityStatus status;

    @Column(columnDefinition = "TEXT")
    private String adminRemark;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private User organization;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Default no-argument constructor required by JPA/Hibernate.
     */
    public Opportunity() {
    }

    /**
     * Parameterized constructor for creating a new opportunity.
     */
    public Opportunity(String title, String description, String location, LocalDate eventDate,
                       LocalTime startTime, LocalTime endTime, int totalSlots,
                       OpportunityStatus status, User organization) {
        this.title = title;
        this.description = description;
        this.location = location;
        this.eventDate = eventDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.totalSlots = totalSlots;
        this.status = status;
        this.organization = organization;
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
        if (this.status == null) {
            this.status = OpportunityStatus.PENDING;
        }
    }

    // ==========================================
    // GETTERS AND SETTERS (Explicit, No Lombok)
    // ==========================================

    /**
     * Returns the unique database ID of the opportunity.
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the database ID of the opportunity.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Returns the title or headline of the volunteering event.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title or headline of the volunteering event.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Returns the detailed description and responsibilities of the event.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the detailed description and responsibilities of the event.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the venue or address where the volunteering takes place.
     */
    public String getLocation() {
        return location;
    }

    /**
     * Sets the venue or address where the volunteering takes place.
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Returns the date scheduled for the volunteering event.
     */
    public LocalDate getEventDate() {
        return eventDate;
    }

    /**
     * Sets the date scheduled for the volunteering event.
     */
    public void setEventDate(LocalDate eventDate) {
        this.eventDate = eventDate;
    }

    /**
     * Returns the start time of the event.
     */
    public LocalTime getStartTime() {
        return startTime;
    }

    /**
     * Sets the start time of the event.
     */
    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    /**
     * Returns the end time of the event.
     */
    public LocalTime getEndTime() {
        return endTime;
    }

    /**
     * Sets the end time of the event.
     */
    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    /**
     * Returns the total volunteer capacity/slots for this opportunity.
     */
    public int getTotalSlots() {
        return totalSlots;
    }

    /**
     * Sets the total volunteer capacity/slots for this opportunity.
     */
    public void setTotalSlots(int totalSlots) {
        this.totalSlots = totalSlots;
    }

    /**
     * Returns the review status (PENDING, APPROVED, REJECTED).
     */
    public OpportunityStatus getStatus() {
        return status;
    }

    /**
     * Sets the review status of the opportunity.
     */
    public void setStatus(OpportunityStatus status) {
        this.status = status;
    }

    /**
     * Returns the feedback remark provided by the Admin during review.
     */
    public String getAdminRemark() {
        return adminRemark;
    }

    /**
     * Sets the feedback remark provided by the Admin during review.
     */
    public void setAdminRemark(String adminRemark) {
        this.adminRemark = adminRemark;
    }

    /**
     * Returns the Organization user who published this opportunity.
     */
    public User getOrganization() {
        return organization;
    }

    /**
     * Sets the Organization user who published this opportunity.
     */
    public void setOrganization(User organization) {
        this.organization = organization;
    }

    /**
     * Returns the timestamp when this opportunity record was created.
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the timestamp when this opportunity record was created.
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
