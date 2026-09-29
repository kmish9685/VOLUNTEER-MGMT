package com.volunteer.platform.config;

import com.volunteer.platform.model.Role;
import com.volunteer.platform.model.User;
import com.volunteer.platform.model.UserStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * =====================================================================
 * LoginInterceptor (Security / Interceptor Layer)
 * ---------------------------------------------------------------------
 * Provides clean, session-based role authorization without the bloat
 * of full Spring Security.
 *
 * Rules:
 * - /admin/**     -> Accessible ONLY by users with role ADMIN
 * - /org/**       -> Accessible ONLY by users with role ORGANIZATION
 * - /volunteer/** -> Accessible ONLY by users with role VOLUNTEER
 * - /messages/**  -> Accessible by ANY logged-in user
 *
 * If unauthenticated: redirects to /login.
 * If logged in with incorrect role: redirects to the user's own dashboard.
 * If blocked: invalidates session and redirects to /login with error message.
 * =====================================================================
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {

    /**
     * Intercepts HTTP requests before they reach the controller to verify login and role authorization.
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        HttpSession session = request.getSession(false);

        User loggedInUser = (session != null) ? (User) session.getAttribute("loggedInUser") : null;

        // If not logged in, redirect to login page
        if (loggedInUser == null) {
            response.sendRedirect(request.getContextPath() + "/login?error=auth_required");
            return false;
        }

        // If user was blocked while session is active, terminate session immediately
        if (loggedInUser.getStatus() == UserStatus.BLOCKED) {
            session.invalidate();
            response.sendRedirect(request.getContextPath() + "/login?error=account_blocked");
            return false;
        }

        Role role = loggedInUser.getRole();

        // Enforce /admin/** restricted access
        if (uri.startsWith("/admin") && role != Role.ADMIN) {
            redirectToOwnDashboard(request, response, role);
            return false;
        }

        // Enforce /org/** restricted access
        if (uri.startsWith("/org") && role != Role.ORGANIZATION) {
            redirectToOwnDashboard(request, response, role);
            return false;
        }

        // Enforce /volunteer/** restricted access
        if (uri.startsWith("/volunteer") && role != Role.VOLUNTEER) {
            redirectToOwnDashboard(request, response, role);
            return false;
        }

        // /messages/** is accessible by any authenticated active user
        return true;
    }

    /**
     * Helper to safely redirect a user to their dedicated dashboard based on role.
     */
    private void redirectToOwnDashboard(HttpServletRequest request, HttpServletResponse response, Role role) throws Exception {
        String contextPath = request.getContextPath();
        if (role == Role.ADMIN) {
            response.sendRedirect(contextPath + "/admin/dashboard");
        } else if (role == Role.ORGANIZATION) {
            response.sendRedirect(contextPath + "/org/dashboard");
        } else if (role == Role.VOLUNTEER) {
            response.sendRedirect(contextPath + "/volunteer/dashboard");
        } else {
            response.sendRedirect(contextPath + "/login");
        }
    }
}
