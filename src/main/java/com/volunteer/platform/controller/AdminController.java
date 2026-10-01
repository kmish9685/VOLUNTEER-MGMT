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
 * - KPI Metrics Dashboard
 * - User Management (Create, Edit, Block/Unblock, Delete)
 * - Review Opportunities (Approve or Reject with Admin Remarks)
 *   Shows PENDING on top, all others below — one combined page
 * - Monitoring: all registrations + recent activity log — one page
 * - System Settings (3 settings: platform_name, allow_registrations, max_hours_per_log)
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
     * Displays the Admin Dashboard with simple KPI counts.
     */
    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        model.addAttribute("totalVolunteers", userService.countVolunteers());
        model.addAttribute("totalOrganizations", userService.countOrganizations());
        model.addAttribute("pendingOpportunitiesCount", opportunityService.countByStatus(OpportunityStatus.PENDING));
        model.addAttribute("approvedOpportunitiesCount", opportunityService.countByStatus(OpportunityStatus.APPROVED));
        model.addAttribute("totalRegistrations", registrationService.countTotalRegistrations());
        model.addAttribute("totalHours", hourLogService.getTotalHoursPlatform());
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
     * Displays all opportunities: PENDING on top, others below — combined review page.
     */
    @GetMapping("/opportunities/review")
    public String showReviewOpportunities(Model model) {
        model.addAttribute("pendingOpportunities", opportunityService.getPendingOpportunities());
        model.addAttribute("allOpportunities", opportunityService.getAllOpportunities());
        return "admin/review-opportunities";
    }

    /**
     * Redirect old /admin/opportunities URL to the review page.
     */
    @GetMapping("/opportunities")
    public String redirectToReview() {
        return "redirect:/admin/opportunities/review";
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
     * Displays the Monitoring page: all registrations + recent activity log.
     * Replaces the old separate Participation and Activity Logs pages.
     */
    @GetMapping("/monitoring")
    public String showMonitoring(Model model) {
        model.addAttribute("registrations", registrationService.getAllRegistrations());
        model.addAttribute("logs", activityLogService.getAllLogs());
        return "admin/monitoring";
    }

    /**
     * Redirect old /admin/participation URL to monitoring.
     */
    @GetMapping("/participation")
    public String redirectParticipation() {
        return "redirect:/admin/monitoring";
    }

    /**
     * Redirect old /admin/activity-logs URL to monitoring.
     */
    @GetMapping("/activity-logs")
    public String redirectActivityLogs() {
        return "redirect:/admin/monitoring";
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
