package com.volunteer.platform.repository;

import com.volunteer.platform.model.Role;
import com.volunteer.platform.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * =====================================================================
 * UserRepository (Data Access Layer)
 * ---------------------------------------------------------------------
 * Handles all database operations for the User entity via Spring Data JPA.
 * Contains methods to find users by email and count users for dashboard reporting.
 * =====================================================================
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user account by their unique login email address.
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks if any account is already registered with the specified email.
     */
    boolean existsByEmail(String email);

    /**
     * Counts the total number of users registered under a specific role.
     */
    long countByRole(Role role);
}
