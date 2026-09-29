package com.volunteer.platform.model;

/**
 * =====================================================================
 * HourLogStatus Enum (Model Layer)
 * ---------------------------------------------------------------------
 * Tracks the approval state of volunteer service hours logged by a student:
 * - PENDING: Volunteer submitted hours; waiting for organization review.
 * - APPROVED: Organization verified the work; hours count toward total.
 * - REJECTED: Organization declined the submitted log entry.
 * =====================================================================
 */
public enum HourLogStatus {
    PENDING,
    APPROVED,
    REJECTED
}
