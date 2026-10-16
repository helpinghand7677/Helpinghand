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
 * POST /api/admin/login
 * Body: { "username": "...", "password": "..." }
 *
 * Same demo credentials that used to be hard-coded in the HTML file,
 * just moved server-side so they no longer sit in plain view in the page
 * source. For a real deployment, replace this with a hashed row in the
 * database, same as UserDAO does for customers.
 */
@WebServlet("/api/admin/login")
public class AdminLoginServlet extends HttpServlet {

    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "gaurabhjha0404";

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Map body = JsonUtil.readBody(req, Map.class);
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
