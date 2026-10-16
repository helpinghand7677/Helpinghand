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
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Technician dashboard API. All endpoints need a technician session (TechnicianAuthServlet).
 *
 * GET  /api/technician/jobs           -> { "available": [...], "mine": [...] }
 *        available = unassigned, Confirmed bookings for this technician's service in their area
 *                    (customer phone / full address / GPS are hidden until the job is accepted)
 *        mine      = jobs this technician accepted (Accepted first, then Completed/Cancelled)
 * POST /api/technician/jobs/accept    Body: { "id": 12 }  -> 200, or 409 if someone else got it first
 * POST /api/technician/jobs/complete  Body: { "id": 12 }  -> 200, or 409 if not your active job
 */
@WebServlet({"/api/technician/jobs", "/api/technician/jobs/accept", "/api/technician/jobs/complete"})
public class TechnicianJobServlet extends HttpServlet {

    private final BookingDAO bookingDAO = new BookingDAO();
    private final TechnicianDAO technicianDAO = new TechnicianDAO();

    /** The logged-in technician, or null after writing a 401 response. */
    private Technician requireTechnician(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Integer id = TechnicianAuthServlet.currentTechnicianId(req);
        Technician t = id == null ? null : technicianDAO.getById(id);
        if (t == null) JsonUtil.writeError(resp, 401, "Technician login required.");
        return t;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Technician t = requireTechnician(req, resp);
        if (t == null) return;

        // Privacy: until a technician accepts a job they only see what they need to decide
        // (service, area, slot, issue). Phone, full address and GPS point are released on accept.
        List<Booking> available = bookingDAO.getAvailableForTechnician(t.getServiceId(), t.getAreaName());
        for (Booking b : available) {
            b.setPhone(null);
            b.setAddress(null);
            b.setLatitude(null);
            b.setLongitude(null);
            String full = b.getCustomerName();
            if (full != null && full.contains(" ")) b.setCustomerName(full.substring(0, full.indexOf(' ')));
        }

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("available", available);
        out.put("mine", bookingDAO.getByTechnician(t.getId()));
        JsonUtil.writeJson(resp, 200, out);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Technician t = requireTechnician(req, resp);
        if (t == null) return;

        Map body = JsonUtil.readBody(req, Map.class);
        Object idObj = body == null ? null : body.get("id");
        if (idObj == null) {
            JsonUtil.writeError(resp, 400, "id is required.");
            return;
        }
        int bookingId;
        try {
            bookingId = (int) Double.parseDouble(idObj.toString()); // Gson parses numbers as Double
        } catch (NumberFormatException e) {
            JsonUtil.writeError(resp, 400, "id must be a number.");
            return;
        }

        if (req.getRequestURI().endsWith("/accept")) {
            if (bookingDAO.acceptJob(bookingId, t.getId(), t.getServiceId(), t.getAreaName())) {
                JsonUtil.writeJson(resp, 200, Map.of("success", true));
            } else {
                JsonUtil.writeError(resp, 409, "This job is no longer available (taken by another technician or cancelled).");
            }
        } else {
            if (bookingDAO.completeJob(bookingId, t.getId())) {
                JsonUtil.writeJson(resp, 200, Map.of("success", true));
            } else {
                JsonUtil.writeError(resp, 409, "Only your accepted, active jobs can be marked completed.");
            }
        }
    }
}
