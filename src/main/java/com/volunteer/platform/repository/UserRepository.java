package com.volunteer.platform.repository;

import com.volunteer.platform.model.Role;
import com.volunteer.platform.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * =====================================================================
 * UserRepository (Data Access Layer)
 * ---------------------------------------------------------------------
 * Handles all database operations for the User entity via Spring Data JPA.
 * Contains methods to find users by email, filter by role, search by keyword,
 * and count users for dashboard reporting.
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
     * Retrieves all users having a specific role (ADMIN, ORGANIZATION, VOLUNTEER).
     */
    List<User> findByRole(Role role);

    /**
     * Counts the total number of users registered under a specific role.
     */
    long countByRole(Role role);

    /**
     * Searches users across full name and email matching the search keyword.
     */
    List<User> findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String name, String email);

    /**
     * Searches users filtered by role and matching a search keyword in name or email.
     */
    @Query("SELECT u FROM User u WHERE u.role = :role AND (LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<User> searchByRoleAndKeyword(@Param("role") Role role, @Param("search") String search);
}
