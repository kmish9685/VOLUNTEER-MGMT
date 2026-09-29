package com.volunteer.platform.controller;

import com.volunteer.platform.model.*;
import com.volunteer.platform.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * =====================================================================
 * AdminController (Presentation Layer)
 * ---------------------------------------------------------------------
 * Handles all administrative operations (/admin/**):
 * - KPI Metrics Dashboard and latest audit feed
 * - User Management (Create, Edit, Block/Unblock, Delete)
 * - Review Opportunities (Approve or Reject with Admin Remarks)
 * - Global Participation Monitoring (All registrations)
 * - System Settings Management (Platform name, limits, flags)
 * - Complete Activity Log audit trail
 * =====================================================================
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final OpportunityService opportunityService;
    private final RegistrationService registrationService;
    private final HourLogService hourLogService;
    private final SettingService settingService;
    private final ActivityLogService activityLogService;

    /**
     * Injects all required business services.
     */
    public AdminController(UserService userService,
                           OpportunityService opportunityService,
                           RegistrationService registrationService,
                           HourLogService hourLogService,
                           SettingService settingService,
                           ActivityLogService activityLogService) {
        this.userService = userService;
        this.opportunityService = opportunityService;
        this.registrationService = registrationService;
        this.hourLogService = hourLogService;
        this.settingService = settingService;
        this.activityLogService = activityLogService;
    }

    /**
     * Displays the Admin Dashboard with summary KPI counters and recent activity logs.
     */
    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        model.addAttribute("totalVolunteers", userService.countVolunteers());
        model.addAttribute("totalOrganizations", userService.countOrganizations());
        model.addAttribute("pendingOpportunitiesCount", opportunityService.countByStatus(OpportunityStatus.PENDING));
        model.addAttribute("approvedOpportunitiesCount", opportunityService.countByStatus(OpportunityStatus.APPROVED));
        model.addAttribute("rejectedOpportunitiesCount", opportunityService.countByStatus(OpportunityStatus.REJECTED));
        model.addAttribute("totalRegistrations", registrationService.countTotalRegistrations());
        model.addAttribute("totalApprovedHours", hourLogService.getTotalApprovedHoursPlatform());
        model.addAttribute("recentLogs", activityLogService.getLatestLogs());
        return "admin/dashboard";
    }

    /**
     * Displays the User Management page with search and role filtering.
     */
    @GetMapping("/users")
    public String listUsers(@RequestParam(value = "role", required = false) Role role,
                            @RequestParam(value = "search", required = false) String search,
                            Model model) {
        List<User> users = userService.searchAndFilterUsers(role, search);
        model.addAttribute("users", users);
        model.addAttribute("selectedRole", role);
        model.addAttribute("searchQuery", search);
        return "admin/users";
    }

    /**
     * Displays the form for creating a new user account by the Admin.
     */
    @GetMapping("/users/new")
    public String showCreateUserForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("roles", Role.values());
        model.addAttribute("isEdit", false);
        return "admin/user-form";
    }

    /**
     * Processes creation of a new user account by the Admin.
     */
    @PostMapping("/users/new")
    public String processCreateUser(@RequestParam("fullName") String fullName,
                                    @RequestParam("email") String email,
                                    @RequestParam("password") String password,
                                    @RequestParam(value = "phone", required = false) String phone,
                                    @RequestParam("role") Role role,
                                    @RequestParam(value = "organizationDescription", required = false) String orgDesc,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {
        try {
            User admin = (User) session.getAttribute("loggedInUser");
            userService.createUserByAdmin(fullName, email, password, phone, role, orgDesc, admin);
            redirectAttributes.addFlashAttribute("successMessage", "User account created successfully!");
            return "redirect:/admin/users";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/admin/users/new";
        }
    }

    /**
     * Displays the form for editing an existing user.
     */
    @GetMapping("/users/edit/{id}")
    public String showEditUserForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        return userService.findById(id).map(user -> {
            model.addAttribute("user", user);
            model.addAttribute("roles", Role.values());
            model.addAttribute("isEdit", true);
            return "admin/user-form";
        }).orElseGet(() -> {
            redirectAttributes.addFlashAttribute("errorMessage", "User not found.");
            return "redirect:/admin/users";
        });
    }

    /**
     * Processes profile updates for an existing user.
     */
    @PostMapping("/users/edit/{id}")
    public String processEditUser(@PathVariable("id") Long id,
                                  @RequestParam("fullName") String fullName,
                                  @RequestParam("email") String email,
                                  @RequestParam(value = "phone", required = false) String phone,
                                  @RequestParam("role") Role role,
                                  @RequestParam(value = "organizationDescription", required = false) String orgDesc,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        try {
            User admin = (User) session.getAttribute("loggedInUser");
            userService.updateUserByAdmin(id, fullName, email, phone, role, orgDesc, admin);
            redirectAttributes.addFlashAttribute("successMessage", "User details updated successfully!");
            return "redirect:/admin/users";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/admin/users/edit/" + id;
        }
    }

    /**
     * Toggles a user's status between ACTIVE and BLOCKED.
     */
    @PostMapping("/users/toggle-status/{id}")
    public String toggleUserStatus(@PathVariable("id") Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            User admin = (User) session.getAttribute("loggedInUser");
            userService.toggleUserStatus(id, admin);
            redirectAttributes.addFlashAttribute("successMessage", "User status updated successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/users";
    }

    /**
     * Deletes a user account from the system.
     */
    @PostMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable("id") Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            User admin = (User) session.getAttribute("loggedInUser");
            userService.deleteUser(id, admin);
            redirectAttributes.addFlashAttribute("successMessage", "User deleted successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/users";
    }

    /**
     * Lists all opportunities waiting for Admin review (status PENDING).
     */
    @GetMapping("/opportunities/review")
    public String showPendingOpportunities(Model model) {
        model.addAttribute("pendingOpportunities", opportunityService.getPendingOpportunities());
        return "admin/review-opportunities";
    }

    /**
     * Processes Admin review decision (APPROVE or REJECT with optional remark).
     */
    @PostMapping("/opportunities/review/{id}")
    public String reviewOpportunity(@PathVariable("id") Long id,
                                    @RequestParam("status") OpportunityStatus status,
                                    @RequestParam(value = "adminRemark", required = false) String remark,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {
        try {
            User admin = (User) session.getAttribute("loggedInUser");
            opportunityService.reviewOpportunity(id, status, remark, admin);
            redirectAttributes.addFlashAttribute("successMessage", "Opportunity " + status + " successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/opportunities/review";
    }

    /**
     * Displays all opportunities across all statuses for comprehensive oversight.
     */
    @GetMapping("/opportunities")
    public String showAllOpportunities(Model model) {
        model.addAttribute("opportunities", opportunityService.getAllOpportunities());
        return "admin/opportunities";
    }

    /**
     * Displays all volunteer registrations across all events for participation monitoring.
     */
    @GetMapping("/participation")
    public String showParticipation(Model model) {
        model.addAttribute("registrations", registrationService.getAllRegistrations());
        return "admin/participation";
    }

    /**
     * Displays the complete system activity log history.
     */
    @GetMapping("/activity-logs")
    public String showActivityLogs(Model model) {
        model.addAttribute("logs", activityLogService.getAllLogs());
        return "admin/activity-logs";
    }

    /**
     * Displays all configurable system settings for editing.
     */
    @GetMapping("/settings")
    public String showSettings(Model model) {
        model.addAttribute("settings", settingService.getAllSettings());
        return "admin/settings";
    }

    /**
     * Updates an individual system setting value.
     */
    @PostMapping("/settings/update")
    public String updateSetting(@RequestParam("settingKey") String key,
                                @RequestParam("settingValue") String value,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        try {
            User admin = (User) session.getAttribute("loggedInUser");
            settingService.updateSetting(key, value);
            activityLogService.log(admin, "Admin updated system setting: " + key + " = " + value);
            redirectAttributes.addFlashAttribute("successMessage", "Setting [" + key + "] updated successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/settings";
    }
}
