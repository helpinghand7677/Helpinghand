package com.helpinghand.servlet;

import com.helpinghand.dao.AreaDAO;
import com.helpinghand.dao.ServiceDAO;
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
 * GET  /api/admin/technicians          -> all registered technicians
 * POST /api/admin/technicians          -> register a technician
 *      Body: { "name": "...", "phone": "9876543210", "serviceId": "plumbing", "areaId": 3 }
 * POST /api/admin/technicians/delete   -> Body: { "id": 5 }
 * All require an admin session (see AdminLoginServlet).
 */
@WebServlet({"/api/admin/technicians", "/api/admin/technicians/delete"})
public class AdminTechnicianServlet extends HttpServlet {

    private final TechnicianDAO technicianDAO = new TechnicianDAO();
    private final ServiceDAO serviceDAO = new ServiceDAO();
    private final AreaDAO areaDAO = new AreaDAO();

    private boolean isAdmin(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        return session != null && Boolean.TRUE.equals(session.getAttribute("isAdmin"));
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!isAdmin(req)) {
            JsonUtil.writeError(resp, 403, "Admin login required.");
            return;
        }
        JsonUtil.writeJson(resp, 200, technicianDAO.getAll());
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

        if (req.getRequestURI().endsWith("/delete")) {
            handleDelete(body, resp);
        } else {
            handleRegister(body, resp);
        }
    }

    private void handleRegister(Map body, HttpServletResponse resp) throws IOException {
        String name = (String) body.get("name");
        String phone = (String) body.get("phone");
        String serviceId = (String) body.get("serviceId");
        Object areaObj = body.get("areaId");

        if (name == null || name.isBlank() || phone == null || phone.isBlank()
                || serviceId == null || serviceId.isBlank() || areaObj == null) {
            JsonUtil.writeError(resp, 400, "name, phone, serviceId and areaId (location) are required.");
            return;
        }

        name = name.trim();
        phone = phone.replaceAll("[\\s+-]", "");
        if (phone.startsWith("91") && phone.length() == 12) phone = phone.substring(2);
        if (!phone.matches("[6-9][0-9]{9}")) {
            JsonUtil.writeError(resp, 400, "Enter a valid 10-digit mobile number.");
            return;
        }

        int areaId = (int) Double.parseDouble(areaObj.toString()); // Gson parses numbers as Double

        if (serviceDAO.getById(serviceId) == null) {
            JsonUtil.writeError(resp, 404, "Service not found.");
            return;
        }
        if (!areaDAO.exists(areaId)) {
            JsonUtil.writeError(resp, 404, "Selected location not found.");
            return;
        }
        if (technicianDAO.phoneExists(phone)) {
            JsonUtil.writeError(resp, 409, "A technician with this phone number is already registered.");
            return;
        }

        int newId = technicianDAO.register(name, phone, serviceId, areaId);
        if (newId == -1) {
            JsonUtil.writeError(resp, 500, "Could not register technician. Please try again.");
            return;
        }
        Technician created = technicianDAO.getById(newId);
        JsonUtil.writeJson(resp, 201, created);
    }

    private void handleDelete(Map body, HttpServletResponse resp) throws IOException {
        Object idObj = body.get("id");
        if (idObj == null) {
            JsonUtil.writeError(resp, 400, "id is required.");
            return;
        }
        int id = (int) Double.parseDouble(idObj.toString());
        if (technicianDAO.delete(id)) {
            JsonUtil.writeJson(resp, 200, Map.of("success", true));
        } else {
            JsonUtil.writeError(resp, 404, "Technician not found.");
        }
    }
}
