package com.volunteer.platform.config;

import com.volunteer.platform.BaseIntegrationTest;
import com.volunteer.platform.model.Role;
import com.volunteer.platform.model.User;
import com.volunteer.platform.model.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * =====================================================================
 * SecurityTest (Web layer tests with MockMvc)
 * ---------------------------------------------------------------------
 * MockMvc sends fake HTTP requests to the app without opening a browser.
 * Checks:
 *  - POST without a CSRF token is rejected (403)
 *  - login with a CSRF token works and CHANGES the session id
 *  - pages need login, and each role only opens its own pages
 *  - a blocked user is logged out on the next click
 * =====================================================================
 */
class SecurityTest extends BaseIntegrationTest {

    private static final String PASSWORD = "secret123";

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("POST without CSRF token is rejected with 403 Forbidden")
    void postWithoutCsrfTokenIsRejected() throws Exception {
        User volunteer = createUser(Role.VOLUNTEER, PASSWORD);

        mockMvc.perform(post("/login")
                        .param("email", volunteer.getEmail())
                        .param("password", PASSWORD))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Login with CSRF token works, opens the right dashboard and changes the session id")
    void loginWithCsrfTokenWorksAndChangesSessionId() throws Exception {
        User volunteer = createUser(Role.VOLUNTEER, PASSWORD);
        MockHttpSession session = new MockHttpSession();
        String sessionIdBeforeLogin = session.getId();

        mockMvc.perform(post("/login")
                        .session(session)
                        .with(csrf())
                        .param("email", volunteer.getEmail())
                        .param("password", PASSWORD))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/volunteer/dashboard"));

        assertNotNull(session.getAttribute("loggedInUser"), "User should be stored in the session");
        assertNotEquals(sessionIdBeforeLogin, session.getId(), "Session id must change at login");
    }

    @Test
    @DisplayName("Wrong password does not log the user in")
    void wrongPasswordIsRejected() throws Exception {
        User volunteer = createUser(Role.VOLUNTEER, PASSWORD);

        mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("email", volunteer.getEmail())
                        .param("password", "wrong-password"))
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @DisplayName("Protected page redirects to login when nobody is logged in")
    void protectedPageNeedsLogin() throws Exception {
        mockMvc.perform(get("/volunteer/dashboard"))
                .andExpect(redirectedUrl("/login?error=auth_required"));
    }

    @Test
    @DisplayName("A volunteer cannot open admin pages")
    void volunteerCannotOpenAdminPages() throws Exception {
        User volunteer = createUser(Role.VOLUNTEER);

        mockMvc.perform(get("/admin/dashboard").sessionAttr("loggedInUser", volunteer))
                .andExpect(redirectedUrl("/volunteer/dashboard"));
    }

    @Test
    @DisplayName("A user blocked by the admin is logged out on the next click")
    void blockedUserIsLoggedOutImmediately() throws Exception {
        User volunteer = createUser(Role.VOLUNTEER);
        volunteer.setStatus(UserStatus.BLOCKED);
        userRepository.save(volunteer);

        mockMvc.perform(get("/volunteer/dashboard").sessionAttr("loggedInUser", volunteer))
                .andExpect(redirectedUrl("/login?error=account_blocked"));
    }
}
