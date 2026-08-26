package com.helpinghand.servlet;

import com.helpinghand.dao.UserDAO;
import com.helpinghand.model.User;
import com.helpinghand.util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * POST /api/register
 * Body: { "name": "...", "email": "...", "phone": "...", "password": "..." }
 */
@WebServlet("/api/register")
public class RegisterServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Map body = JsonUtil.readBody(req, Map.class);
        String name = (String) body.get("name");
        String email = (String) body.get("email");
        String phone = (String) body.get("phone");
        String password = (String) body.get("password");

        if (name == null || email == null || phone == null || password == null
                || name.isBlank() || email.isBlank() || phone.isBlank() || password.isBlank()) {
            JsonUtil.writeError(resp, 400, "All fields (name, email, phone, password) are required.");
            return;
        }

        int newId = userDAO.registerUser(name.trim(), email.trim().toLowerCase(), phone.trim(), password);
        if (newId == -1) {
            JsonUtil.writeError(resp, 409, "An account with this email already exists.");
            return;
        }

        User created = userDAO.findByEmail(email.trim().toLowerCase());
        JsonUtil.writeJson(resp, 201, created);
    }
}
