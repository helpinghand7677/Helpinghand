package com.helpinghand.servlet;

import com.helpinghand.util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;

/**
 * POST /api/admin/login    Body: { "username": "...", "password": "..." }
 * POST /api/admin/logout
 * GET  /api/admin/me       -> 200 if the current session is an admin, else 401
 *
 * Credentials default to the values below but can be overridden (recommended)
 * with the ADMIN_USERNAME / ADMIN_PASSWORD environment variables.
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
            JsonUtil.writeError(resp, 401, "Not logged in.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (req.getServletPath().endsWith("/logout")) {
            HttpSession existing = req.getSession(false);
            if (existing != null) existing.removeAttribute("isAdmin");
            JsonUtil.writeJson(resp, 200, Map.of("success", true));
            return;
        }

        Map body = JsonUtil.readBody(req, Map.class);
        if (body == null) {
            JsonUtil.writeError(resp, 400, "Request body is required.");
            return;
        }
        String username = (String) body.get("username");
        String password = (String) body.get("password");

        if (ADMIN_USERNAME.equals(username) && ADMIN_PASSWORD.equals(password)) {
            HttpSession session = req.getSession(true);
            session.setAttribute("isAdmin", true);
            JsonUtil.writeJson(resp, 200, Map.of("success", true));
        } else {
            JsonUtil.writeError(resp, 401, "Invalid admin credentials.");
        }
    }
}
