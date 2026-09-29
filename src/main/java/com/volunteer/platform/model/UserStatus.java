package com.volunteer.platform.model;

/**
 * =====================================================================
 * UserStatus Enum (Model Layer)
 * ---------------------------------------------------------------------
 * Represents the account standing of any user:
 * - ACTIVE: Normal operational state; user can log in and perform actions.
 * - BLOCKED: User is suspended by the Admin; login attempts are rejected.
 * =====================================================================
 */
public enum UserStatus {
    ACTIVE,
    BLOCKED
}
