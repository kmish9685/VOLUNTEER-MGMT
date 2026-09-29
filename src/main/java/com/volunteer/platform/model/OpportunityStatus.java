package com.volunteer.platform.model;

/**
 * =====================================================================
 * OpportunityStatus Enum (Model Layer)
 * ---------------------------------------------------------------------
 * Represents the lifecycle review state of a volunteering opportunity:
 * - PENDING: Awaiting review by the Admin (or auto-approved based on setting).
 * - APPROVED: Approved by Admin; visible to volunteers for registration.
 * - REJECTED: Rejected by Admin with an explanation remark.
 * =====================================================================
 */
public enum OpportunityStatus {
    PENDING,
    APPROVED,
    REJECTED
}
