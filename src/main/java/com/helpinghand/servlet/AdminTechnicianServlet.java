package com.helpinghand.servlet;

import com.helpinghand.dao.TechnicianDAO;
import com.helpinghand.model.Technician;
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
 * GET  /api/admin/technicians          -> list all technicians
 * POST /api/admin/technicians          -> register. Body: { name, phone, location, skill, password }
 * POST /api/admin/technicians/delete   -> Body: { "id": 3 }
 * All require an admin session (see AdminLoginServlet).
 */
@WebServlet({"/api/admin/technicians", "/api/admin/technicians/delete"})
public class AdminTechnicianServlet extends HttpServlet {

    private final TechnicianDAO dao = new TechnicianDAO();

    private boolean isAdmin(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        return session != null && Boolean.TRUE.equals(session.getAttribute("isAdmin"));
    }

    private static String str(Map body, String key) {
        Object v = body.get(key);
        return v == null ? "" : v.toString().trim();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!isAdmin(req)) {
            JsonUtil.writeError(resp, 403, "Admin login required.");
            return;
        }
        JsonUtil.writeJson(resp, 200, dao.getAll());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!isAdmin(req)) {
            JsonUtil.writeError(resp, 403, "Admin login required.");
            return;
        }
        Map body = JsonUtil.readBody(req, Map.class);
        if (body == null) {
            JsonUtil.writeError(resp, 400, "Request body is required.");
            return;
        }

        if (req.getServletPath().endsWith("/delete")) {
            String idStr = str(body, "id");
            if (idStr.isEmpty()) {
                JsonUtil.writeError(resp, 400, "id is required.");
                return;
            }
            int id = (int) Double.parseDouble(idStr);
            if (dao.delete(id)) JsonUtil.writeJson(resp, 200, Map.of("success", true));
            else JsonUtil.writeError(resp, 404, "Technician not found.");
            return;
        }

        String name = str(body, "name");
        String phone = str(body, "phone");
        String location = str(body, "location");
        String skill = str(body, "skill");
        String password = body.get("password") == null ? "" : body.get("password").toString();

        if (name.isEmpty() || phone.isEmpty() || location.isEmpty() || skill.isEmpty() || password.isEmpty()) {
            JsonUtil.writeError(resp, 400, "All fields (name, phone, location, skill, password) are required.");
            return;
        }
        if (!phone.matches("\\d{10}")) {
            JsonUtil.writeError(resp, 400, "Phone number must be 10 digits.");
            return;
        }
        if (password.length() < 6) {
            JsonUtil.writeError(resp, 400, "Password must be at least 6 characters.");
            return;
        }

        int id = dao.register(name, phone, location, skill, password);
        if (id == -1) {
            JsonUtil.writeError(resp, 409, "A technician with this phone number already exists.");
            return;
        }
        JsonUtil.writeJson(resp, 201, dao.findByPhone(phone));
    }
}
