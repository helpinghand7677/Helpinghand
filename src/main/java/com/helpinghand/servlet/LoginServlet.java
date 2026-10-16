package com.helpinghand.servlet;

import com.helpinghand.dao.UserDAO;
import com.helpinghand.model.User;
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
 * POST /api/login
 * Body: { "email": "...", "password": "..." }
 * On success, stores the logged-in user's id in the HttpSession too,
 * so other servlets can check req.getSession().getAttribute("userId").
 */
@WebServlet("/api/login")
public class LoginServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Map body = JsonUtil.readBody(req, Map.class);
        String email = (String) body.get("email");
        String password = (String) body.get("password");

        if (email == null || password == null) {
            JsonUtil.writeError(resp, 400, "Email and password are required.");
            return;
        }

        User user = userDAO.login(email.trim().toLowerCase(), password);
        if (user == null) {
            JsonUtil.writeError(resp, 401, "Invalid email or password.");
            return;
        }

        HttpSession session = req.getSession(true);
        session.setAttribute("userId", user.getId());
        session.setAttribute("userEmail", user.getEmail());

        JsonUtil.writeJson(resp, 200, user);
    }
}
