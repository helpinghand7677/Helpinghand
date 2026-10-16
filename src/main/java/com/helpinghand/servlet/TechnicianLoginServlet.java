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
 * POST /api/technician/login   Body: { "phone": "...", "password": "..." }
 * GET  /api/technician/me      -> current logged-in technician (401 if none)
 * POST /api/technician/logout
 */
@WebServlet({"/api/technician/login", "/api/technician/me", "/api/technician/logout"})
public class TechnicianLoginServlet extends HttpServlet {

    private final TechnicianDAO dao = new TechnicianDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (req.getServletPath().endsWith("/logout")) {
            HttpSession s = req.getSession(false);
            if (s != null) s.removeAttribute("technicianPhone");
            JsonUtil.writeJson(resp, 200, Map.of("success", true));
            return;
        }

        Map body = JsonUtil.readBody(req, Map.class);
        String phone = body == null || body.get("phone") == null ? "" : body.get("phone").toString().trim();
        String password = body == null || body.get("password") == null ? "" : body.get("password").toString();
        if (phone.isEmpty() || password.isEmpty()) {
            JsonUtil.writeError(resp, 400, "Phone number and password are required.");
            return;
        }

        Technician t = dao.login(phone, password);
        if (t == null) {
            JsonUtil.writeError(resp, 401, "Invalid phone number or password.");
            return;
        }
        req.getSession(true).setAttribute("technicianPhone", t.getPhone());
        JsonUtil.writeJson(resp, 200, t);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession s = req.getSession(false);
        String phone = s == null ? null : (String) s.getAttribute("technicianPhone");
        Technician t = phone == null ? null : dao.findByPhone(phone);
        if (t == null) {
            JsonUtil.writeError(resp, 401, "Technician login required.");
            return;
        }
        JsonUtil.writeJson(resp, 200, t);
    }
}
