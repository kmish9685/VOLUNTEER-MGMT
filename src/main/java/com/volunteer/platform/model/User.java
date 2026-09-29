package com.volunteer.platform.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * =====================================================================
 * User Entity (Model Layer)
 * ---------------------------------------------------------------------
 * Represents an account in the system across all three roles:
 * - ADMIN: Manages the platform, reviews events, and configures settings.
 * - ORGANIZATION: Hosts volunteering opportunities, verifies attendance & hours.
 * - VOLUNTEER: Browses opportunities, attends events, and logs hours.
 *
 * Stored in the "users" table in MySQL.
 * =====================================================================
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status;

    @Column(columnDefinition = "TEXT")
    private String organizationDescription;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Default no-argument constructor required by JPA/Hibernate.
     */
    public User() {
    }

    /**
     * Parameterized constructor to initialize a new user instance.
     */
    public User(String fullName, String email, String password, String phone, Role role, UserStatus status) {
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.role = role;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Automatically sets createdAt timestamp before saving new entity to database.
     */
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = UserStatus.ACTIVE;
        }
    }

    // ==========================================
    // GETTERS AND SETTERS (Explicit, No Lombok)
    // ==========================================

    /**
     * Returns the unique database ID of the user.
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the database ID of the user.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Returns the full name or organization name of the user.
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Sets the full name or organization name of the user.
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /**
     * Returns the user's login email address.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the user's login email address.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Returns the BCrypt hashed password of the user.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the hashed password of the user.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Returns the contact phone number of the user.
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Sets the contact phone number of the user.
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * Returns the system role of the user (ADMIN, ORGANIZATION, or VOLUNTEER).
     */
    public Role getRole() {
        return role;
    }

    /**
     * Assigns the system role to the user.
     */
    public void setRole(Role role) {
        this.role = role;
    }

    /**
     * Returns the current status of the user (ACTIVE or BLOCKED).
     */
    public UserStatus getStatus() {
        return status;
    }

    /**
     * Sets the current status of the user.
     */
    public void setStatus(UserStatus status) {
        this.status = status;
    }

    /**
     * Returns the organizational mission or bio description (if ORGANIZATION role).
     */
    public String getOrganizationDescription() {
        return organizationDescription;
    }

    /**
     * Sets the organizational mission or bio description.
     */
    public void setOrganizationDescription(String organizationDescription) {
        this.organizationDescription = organizationDescription;
    }

    /**
     * Returns the date and time when the account was registered.
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the creation timestamp of the account.
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
