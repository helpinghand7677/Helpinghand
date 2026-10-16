package com.helpinghand.servlet;

import com.helpinghand.dao.BookingDAO;
import com.helpinghand.dao.TechnicianDAO;
import com.helpinghand.model.Booking;
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
 * GET  /api/technician/bookings         -> { "available": [...], "mine": [...] }
 *      available = new orders matching this technician's skill AND location
 *      mine      = orders this technician already accepted
 * POST /api/technician/bookings/accept  -> Body: { "id": 12 }
 * Both require a technician session (see TechnicianLoginServlet).
 */
@WebServlet({"/api/technician/bookings", "/api/technician/bookings/accept"})
public class TechnicianBookingServlet extends HttpServlet {

    private final BookingDAO bookingDAO = new BookingDAO();
    private final TechnicianDAO technicianDAO = new TechnicianDAO();

    private Technician currentTechnician(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        String phone = s == null ? null : (String) s.getAttribute("technicianPhone");
        return phone == null ? null : technicianDAO.findByPhone(phone);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Technician t = currentTechnician(req);
        if (t == null) {
            JsonUtil.writeError(resp, 401, "Technician login required.");
            return;
        }
        JsonUtil.writeJson(resp, 200, Map.of(
                "available", bookingDAO.getAvailableForTechnician(t.getSkill(), t.getLocation()),
                "mine", bookingDAO.getAcceptedByTechnician(t.getPhone())));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Technician t = currentTechnician(req);
        if (t == null) {
            JsonUtil.writeError(resp, 401, "Technician login required.");
            return;
        }
        Map body = JsonUtil.readBody(req, Map.class);
        Object idObj = body == null ? null : body.get("id");
        if (idObj == null) {
            JsonUtil.writeError(resp, 400, "id is required.");
            return;
        }
        int id = (int) Double.parseDouble(idObj.toString());

        Booking b = bookingDAO.getById(id);
        if (b == null) {
            JsonUtil.writeError(resp, 404, "Booking not found.");
            return;
        }
        // A technician may only accept orders for their own skill and area.
        if (!t.getSkill().equals(b.getServiceName()) || !t.getLocation().equals(b.getLocation())) {
            JsonUtil.writeError(resp, 403, "This order is outside your skill or location.");
            return;
        }
        if (bookingDAO.acceptBooking(id, t.getName(), t.getPhone())) {
            JsonUtil.writeJson(resp, 200, Map.of("success", true));
        } else {
            JsonUtil.writeError(resp, 409, "This order was already accepted by someone else or cancelled.");
        }
    }
}
