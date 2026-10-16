package com.helpinghand.servlet;

import com.helpinghand.dao.TechnicianDAO;
import com.helpinghand.model.Technician;
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
 * POST /api/technician/login   Body: { "phone": "9876543210", "password": "..." }
 *                              -> the technician (id, name, phone, service, area) and a session
 * POST /api/technician/logout  -> ends the technician session
 * GET  /api/technician/me      -> the logged-in technician, or 401 (used to restore the
 *                                 dashboard after a page reload)
 *
 * Technicians are registered (with an initial password) by the admin. The session
 * attribute "technicianId" is what TechnicianJobServlet checks.
 */
@WebServlet({"/api/technician/login", "/api/technician/logout", "/api/technician/me"})
public class TechnicianAuthServlet extends HttpServlet {

    private final TechnicianDAO technicianDAO = new TechnicianDAO();

    static Integer currentTechnicianId(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) return null;
        Object id = session.getAttribute("technicianId");
        return id instanceof Integer ? (Integer) id : null;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Integer id = currentTechnicianId(req);
        Technician t = id == null ? null : technicianDAO.getById(id);
        if (t == null) {
            JsonUtil.writeError(resp, 401, "Technician login required.");
            return;
        }
        JsonUtil.writeJson(resp, 200, t);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (req.getRequestURI().endsWith("/logout")) {
            HttpSession session = req.getSession(false);
            if (session != null) session.removeAttribute("technicianId");
            JsonUtil.writeJson(resp, 200, Map.of("success", true));
            return;
        }

        Map body = JsonUtil.readBody(req, Map.class);
        String phone = body == null ? null : (String) body.get("phone");
        String password = body == null ? null : (String) body.get("password");
        if (phone == null || phone.isBlank() || password == null || password.isEmpty()) {
            JsonUtil.writeError(resp, 400, "Phone and password are required.");
            return;
        }

        phone = phone.replaceAll("[\\s+-]", "");
        if (phone.startsWith("91") && phone.length() == 12) phone = phone.substring(2);

        String throttleKey = req.getRemoteAddr() + "|" + phone;
        if (LoginThrottle.isBlocked(throttleKey)) {
            JsonUtil.writeError(resp, 429, "Too many wrong attempts. Please try again in 10 minutes.");
            return;
        }

        Technician t = technicianDAO.login(phone, password);
        if (t == null) {
            LoginThrottle.recordFailure(throttleKey);
            JsonUtil.writeError(resp, 401, "Invalid phone number or password.");
            return;
        }
        LoginThrottle.recordSuccess(throttleKey);

        HttpSession session = req.getSession(true);
        if (!session.isNew()) req.changeSessionId(); // avoid session fixation
        session.setAttribute("technicianId", t.getId());
        JsonUtil.writeJson(resp, 200, t);
    }
}
