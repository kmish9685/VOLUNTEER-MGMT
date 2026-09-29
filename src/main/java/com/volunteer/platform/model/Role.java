package com.volunteer.platform.model;

/**
 * =====================================================================
 * Role Enum (Model Layer)
 * ---------------------------------------------------------------------
 * Defines the three user roles supported by the volunteer platform:
 * 1. ADMIN - Full system control, approvals, user management, and settings.
 * 2. ORGANIZATION - Creates volunteering events, takes attendance, approves hours.
 * 3. VOLUNTEER - Browses events, signs up, logs volunteer hours, messages orgs.
 * =====================================================================
 */
public enum Role {
    ADMIN,
    ORGANIZATION,
    VOLUNTEER
}
