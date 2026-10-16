package com.helpinghand.servlet;

import com.helpinghand.util.JsonUtil;
import com.helpinghand.util.LoginThrottle;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;

/**
 * POST /api/admin/login   Body: { "username": "...", "password": "..." }
 * POST /api/admin/logout
 * GET  /api/admin/me      -> 200 if the session is an admin session, else 403 (restores the panel on reload)
 *
 * Credentials come from the ADMIN_USERNAME / ADMIN_PASSWORD environment variables
 * (set them in docker-compose.yml); the old demo values remain only as a fallback.
 *
 * Same demo credentials that used to be hard-coded in the HTML file,
 * just moved server-side so they no longer sit in plain view in the page
 * source. For a real deployment, replace this with a hashed row in the
 * database, same as UserDAO does for customers.
 */
@WebServlet({"/api/admin/login", "/api/admin/logout", "/api/admin/me"})
public class AdminLoginServlet extends HttpServlet {

    private static String env(String key, String fallback) {
        String v = System.getenv(key);
        return (v == null || v.isBlank()) ? fallback : v;
    }

    private static final String ADMIN_USERNAME = env("ADMIN_USERNAME", "admin");
    private static final String ADMIN_PASSWORD = env("ADMIN_PASSWORD", "gaurabhjha0404");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session != null && Boolean.TRUE.equals(session.getAttribute("isAdmin"))) {
            JsonUtil.writeJson(resp, 200, Map.of("success", true));
        } else {
            JsonUtil.writeError(resp, 403, "Admin login required.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (req.getRequestURI().endsWith("/logout")) {
            HttpSession session = req.getSession(false);
            if (session != null) session.removeAttribute("isAdmin");
            JsonUtil.writeJson(resp, 200, Map.of("success", true));
            return;
        }

        Map body = JsonUtil.readBody(req, Map.class);
        String username = body == null ? null : (String) body.get("username");
        String password = body == null ? null : (String) body.get("password");

        String throttleKey = "admin|" + req.getRemoteAddr();
        if (LoginThrottle.isBlocked(throttleKey)) {
            JsonUtil.writeError(resp, 429, "Too many wrong attempts. Please try again in 10 minutes.");
            return;
        }

        if (ADMIN_USERNAME.equals(username) && ADMIN_PASSWORD.equals(password)) {
            LoginThrottle.recordSuccess(throttleKey);
            HttpSession session = req.getSession(true);
            if (!session.isNew()) req.changeSessionId(); // avoid session fixation
            session.setAttribute("isAdmin", true);
            JsonUtil.writeJson(resp, 200, Map.of("success", true));
        } else {
            LoginThrottle.recordFailure(throttleKey);
            JsonUtil.writeError(resp, 401, "Invalid admin credentials.");
        }
    }
}
