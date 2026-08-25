package com.helpinghand.servlet;

import com.helpinghand.dao.BookingDAO;
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
 * GET  /api/admin/bookings              -> all bookings (admin panel list)
 * POST /api/admin/bookings/cancel       -> Body: { "id": 12 }
 * Both require an admin session (see AdminLoginServlet).
 */
@WebServlet({"/api/admin/bookings", "/api/admin/bookings/cancel"})
public class AdminBookingServlet extends HttpServlet {

    private final BookingDAO bookingDAO = new BookingDAO();

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
        JsonUtil.writeJson(resp, 200, bookingDAO.getAllBookings());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!isAdmin(req)) {
            JsonUtil.writeError(resp, 403, "Admin login required.");
            return;
        }

        Map body = JsonUtil.readBody(req, Map.class);
        Object idObj = body.get("id");
        if (idObj == null) {
            JsonUtil.writeError(resp, 400, "id is required.");
            return;
        }
        int id = (int) Double.parseDouble(idObj.toString()); // Gson parses numbers as Double

        boolean cancelled = bookingDAO.cancelBooking(id);
        if (cancelled) {
            JsonUtil.writeJson(resp, 200, Map.of("success", true));
        } else {
            JsonUtil.writeError(resp, 404, "Booking not found.");
        }
    }
}
