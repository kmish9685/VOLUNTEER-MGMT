package com.volunteer.platform.service;

import com.volunteer.platform.model.Role;
import com.volunteer.platform.model.User;
import com.volunteer.platform.model.UserStatus;
import com.volunteer.platform.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * =====================================================================
 * UserService (Business Logic Layer)
 * ---------------------------------------------------------------------
 * Handles all user-related business rules:
 * - Registration validation & BCrypt password hashing
 * - Session-based authentication & status checks (ACTIVE vs BLOCKED)
 * - Admin user management (create, update, block/unblock, delete)
 * - Search and role-based filtering for user rosters
 * =====================================================================
 */
@SuppressWarnings("null") // Spring Data JPA's findById(Long) is always called with non-null ids from path variables
@Service
public class UserService {

    private final UserRepository userRepository;
    private final SettingService settingService;
    private final ActivityLogService activityLogService;
    private final PasswordEncoder passwordEncoder;

    /**
     * Constructor for injecting repository, settings, audit, and encoder dependencies.
     */
    public UserService(UserRepository userRepository,
                       SettingService settingService,
                       ActivityLogService activityLogService,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.settingService = settingService;
        this.activityLogService = activityLogService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registers a new user from the public registration form.
     * Enforces that registrations are open, email is unique, and role is either VOLUNTEER or ORGANIZATION.
     */
    @Transactional
    public User registerUser(String fullName, String email, String rawPassword, String phone,
                             Role role, String organizationDescription) {
        boolean allowRegistrations = settingService.getBoolean("allow_registrations", true);
        if (!allowRegistrations) {
            throw new IllegalArgumentException("Registrations are currently closed by the administrator.");
        }

        if (userRepository.existsByEmail(email.trim().toLowerCase())) {
            throw new IllegalArgumentException("An account with email " + email + " already exists.");
        }

        if (role == Role.ADMIN) {
            throw new IllegalArgumentException("Administrator accounts cannot be self-registered.");
        }

        User user = new User();
        user.setFullName(fullName.trim());
        user.setEmail(email.trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setPhone(phone != null ? phone.trim() : null);
        user.setRole(role);
        user.setStatus(UserStatus.ACTIVE);
        if (role == Role.ORGANIZATION && organizationDescription != null) {
            user.setOrganizationDescription(organizationDescription.trim());
        }

        User savedUser = userRepository.save(user);
        activityLogService.log(savedUser, "New user registered: " + savedUser.getFullName() + " (" + savedUser.getRole() + ")");
        return savedUser;
    }

    /**
     * Authenticates a user by email and raw password.
     * Throws an exception if credentials do not match or if the account is BLOCKED.
     */
    public User authenticate(String email, String rawPassword) {
        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password."));

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password.");
        }

        if (user.getStatus() == UserStatus.BLOCKED) {
            throw new IllegalStateException("Your account has been blocked by the administrator.");
        }

        activityLogService.log(user, "User logged in: " + user.getFullName());
        return user;
    }

    /**
     * Finds a user by their database ID.
     */
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Finds a user by their email address.
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email.trim().toLowerCase());
    }

    /**
     * Retrieves all registered users in the platform.
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Searches and filters users by role and keyword (name or email).
     */
    public List<User> searchAndFilterUsers(Role role, String keyword) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            String term = keyword.trim();
            if (role != null) {
                return userRepository.searchByRoleAndKeyword(role, term);
            }
            return userRepository.findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(term, term);
        } else if (role != null) {
            return userRepository.findByRole(role);
        }
        return userRepository.findAll();
    }

    /**
     * Allows an Admin to create a new user account with any assigned role.
     */
    @Transactional
    public User createUserByAdmin(String fullName, String email, String rawPassword, String phone,
                                  Role role, String organizationDescription, User currentAdmin) {
        if (userRepository.existsByEmail(email.trim().toLowerCase())) {
            throw new IllegalArgumentException("An account with email " + email + " already exists.");
        }

        User user = new User();
        user.setFullName(fullName.trim());
        user.setEmail(email.trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setPhone(phone != null ? phone.trim() : null);
        user.setRole(role);
        user.setStatus(UserStatus.ACTIVE);
        if (role == Role.ORGANIZATION && organizationDescription != null) {
            user.setOrganizationDescription(organizationDescription.trim());
        }

        User savedUser = userRepository.save(user);
        activityLogService.log(currentAdmin, "Admin created user: " + savedUser.getFullName() + " (" + savedUser.getRole() + ")");
        return savedUser;
    }

    /**
     * Allows an Admin to update user profile information.
     */
    @Transactional
    public void updateUserByAdmin(Long id, String fullName, String email, String phone,
                                  Role role, String orgDescription, User currentAdmin) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));

        String newEmail = email.trim().toLowerCase();
        if (!user.getEmail().equalsIgnoreCase(newEmail) && userRepository.existsByEmail(newEmail)) {
            throw new IllegalArgumentException("Email " + newEmail + " is already in use by another user.");
        }

        user.setFullName(fullName.trim());
        user.setEmail(newEmail);
        user.setPhone(phone != null ? phone.trim() : null);
        user.setRole(role);
        if (role == Role.ORGANIZATION) {
            user.setOrganizationDescription(orgDescription != null ? orgDescription.trim() : null);
        }

        userRepository.save(user);
        activityLogService.log(currentAdmin, "Admin updated profile for user: " + user.getFullName());
    }

    /**
     * Toggles a user's status between ACTIVE and BLOCKED. Prevents admins from blocking themselves.
     */
    @Transactional
    public void toggleUserStatus(Long userId, User currentAdmin) {
        if (currentAdmin.getId().equals(userId)) {
            throw new IllegalArgumentException("You cannot block your own administrator account.");
        }

        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        if (targetUser.getStatus() == UserStatus.ACTIVE) {
            targetUser.setStatus(UserStatus.BLOCKED);
            activityLogService.log(currentAdmin, "Admin blocked user account: " + targetUser.getEmail());
        } else {
            targetUser.setStatus(UserStatus.ACTIVE);
            activityLogService.log(currentAdmin, "Admin unblocked user account: " + targetUser.getEmail());
        }
        userRepository.save(targetUser);
    }

    /**
     * Deletes a user account. Prevents an administrator from deleting their own account.
     */
    @Transactional
    public void deleteUser(Long userId, User currentAdmin) {
        if (currentAdmin.getId().equals(userId)) {
            throw new IllegalArgumentException("You cannot delete your own administrator account.");
        }

        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        activityLogService.log(currentAdmin, "Admin deleted user: " + targetUser.getFullName() + " (" + targetUser.getEmail() + ")");
        userRepository.delete(targetUser);
    }

    /**
     * Counts the total number of registered volunteers in the platform.
     */
    public long countVolunteers() {
        return userRepository.countByRole(Role.VOLUNTEER);
    }

    /**
     * Counts the total number of registered organizations in the platform.
     */
    public long countOrganizations() {
        return userRepository.countByRole(Role.ORGANIZATION);
    }
}
