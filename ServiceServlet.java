package com.helpinghand.servlet;

import com.helpinghand.dao.ServiceDAO;
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
 * GET  /api/services            -> returns the full service catalog (used to
 *                                   render the Home + "All Services" pages)
 * POST /api/services/update-price
 *      Body: { "id": "ac-repair-and-service", "price": "₹449" }
 *      Requires an admin session (see AdminLoginServlet).
 */
@WebServlet({"/api/services", "/api/services/update-price"})
public class ServiceServlet extends HttpServlet {

    private final ServiceDAO serviceDAO = new ServiceDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        JsonUtil.writeJson(resp, 200, serviceDAO.getAllServices());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        boolean isAdmin = session != null && Boolean.TRUE.equals(session.getAttribute("isAdmin"));
        if (!isAdmin) {
            JsonUtil.writeError(resp, 403, "Admin login required.");
            return;
        }

        Map body = JsonUtil.readBody(req, Map.class);
        String id = (String) body.get("id");
        String price = (String) body.get("price");

        if (id == null || price == null || price.isBlank()) {
            JsonUtil.writeError(resp, 400, "id and price are required.");
            return;
        }

        boolean updated = serviceDAO.updatePrice(id, price);
        if (updated) {
            JsonUtil.writeJson(resp, 200, Map.of("success", true));
        } else {
            JsonUtil.writeError(resp, 404, "Service not found.");
        }
    }
}
