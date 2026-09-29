package com.volunteer.platform.model;

/**
 * =====================================================================
 * RegistrationStatus Enum (Model Layer)
 * ---------------------------------------------------------------------
 * Represents the participation state of a volunteer in a registered event:
 * - REGISTERED: Signed up, waiting for the event date.
 * - ATTENDED: Organization marked the volunteer as present at the event.
 * - ABSENT: Organization marked the volunteer as absent from the event.
 * - CANCELLED: Volunteer withdrew their registration prior to the event date.
 * =====================================================================
 */
public enum RegistrationStatus {
    REGISTERED,
    ATTENDED,
    ABSENT,
    CANCELLED
}
