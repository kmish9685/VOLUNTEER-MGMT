package com.volunteer.platform.config;

import com.volunteer.platform.model.Role;
import com.volunteer.platform.model.User;
import com.volunteer.platform.model.UserStatus;
import com.volunteer.platform.service.UserService;
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

    private final UserService userService;

    /**
     * UserService is injected by Spring (constructor injection) so we can re-check
     * the user's latest status from the database on every request.
     */
    public LoginInterceptor(UserService userService) {
        this.userService = userService;
    }

    /**
     * Intercepts HTTP requests before they reach the controller to verify login and role authorization.
     */
    @Override
    @SuppressWarnings("null") // Inherited @NonNull annotations from HandlerInterceptor; parameters are always non-null at runtime
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        HttpSession session = request.getSession(false);

        User loggedInUser = (session != null) ? (User) session.getAttribute("loggedInUser") : null;

        // If not logged in, redirect to login page
        if (loggedInUser == null) {
            response.sendRedirect(request.getContextPath() + "/login?error=auth_required");
            return false;
        }

        // The session only holds a COPY of the user taken at login time.
        // Re-load the user from the database so that if the admin blocked, deleted,
        // or changed the role of this user, it takes effect immediately.
        User freshUser = userService.findById(loggedInUser.getId()).orElse(null);
        if (freshUser == null) {
            // User was deleted by admin while logged in
            session.invalidate();
            response.sendRedirect(request.getContextPath() + "/login?error=auth_required");
            return false;
        }
        session.setAttribute("loggedInUser", freshUser); // keep session copy up to date
        loggedInUser = freshUser;

        // If user was blocked while session is active, terminate session immediately
        if (loggedInUser.getStatus() == UserStatus.BLOCKED) {
            if (session != null) { // Guard against race condition where session was concurrently invalidated
                session.invalidate();
            }
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
