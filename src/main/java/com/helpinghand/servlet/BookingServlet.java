package com.helpinghand.servlet;

import com.helpinghand.dao.BookingDAO;
import com.helpinghand.dao.ServiceDAO;
import com.helpinghand.model.Booking;
import com.helpinghand.model.ServiceItem;
import com.helpinghand.util.JsonUtil;
import com.helpinghand.util.NotificationUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * GET  /api/bookings?phone=9876543210   -> "My Bookings" list for that phone
 * POST /api/bookings                    -> create a new booking
 * POST /api/bookings/cancel               -> Body: { "id": 12, "phone": "9876543210" } (phone must match the booking)
 *   Body: { "serviceId": "...", "name": "...", "phone": "...", "location": "...", "address": "...",
 *           "date": "yyyy-mm-dd", "time": "HH:mm", "issue": "...", "email": "..." (optional, for email receipt) }
 */
@WebServlet({"/api/bookings", "/api/bookings/cancel"})
public class BookingServlet extends HttpServlet {

    private final BookingDAO bookingDAO = new BookingDAO();
    private final ServiceDAO serviceDAO = new ServiceDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String phone = req.getParameter("phone");
        if (phone == null || phone.isBlank()) {
            JsonUtil.writeError(resp, 400, "phone query parameter is required.");
            return;
        }
        List<Booking> bookings = bookingDAO.getBookingsByPhone(phone.trim());
        JsonUtil.writeJson(resp, 200, bookings);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Map body = JsonUtil.readBody(req, Map.class);
        if (body == null) {
            JsonUtil.writeError(resp, 400, "Request body is required.");
            return;
        }

        if (req.getServletPath().endsWith("/cancel")) {
            Object idObj = body.get("id");
            Object phoneObj = body.get("phone");
            if (idObj == null || phoneObj == null) {
                JsonUtil.writeError(resp, 400, "id and phone are required.");
                return;
            }
            int id = (int) Double.parseDouble(idObj.toString());
            if (bookingDAO.cancelBookingForPhone(id, phoneObj.toString().trim())) {
                JsonUtil.writeJson(resp, 200, Map.of("success", true));
            } else {
                JsonUtil.writeError(resp, 404, "Booking not found.");
            }
            return;
        }

        String serviceId = (String) body.get("serviceId");
        String name = (String) body.get("name");
        String phone = (String) body.get("phone");
        String location = (String) body.get("location");
        String address = (String) body.get("address");
        String date = (String) body.get("date");
        String time = (String) body.get("time");
        String issue = (String) body.get("issue");
        String issueSummary = (String) body.get("issueSummary");
        String email = (String) body.get("email"); // optional

        if (serviceId == null || name == null || phone == null || location == null || location.isBlank()
                || address == null || date == null || time == null) {
            JsonUtil.writeError(resp, 400, "serviceId, name, phone, location, address, date and time are required.");
            return;
        }

        ServiceItem service = serviceDAO.getById(serviceId);
        if (service == null) {
            JsonUtil.writeError(resp, 404, "Service not found.");
            return;
        }

        // Don't allow the same customer to submit the same booking twice.
        if (bookingDAO.isDuplicate(phone.trim(), serviceId, date, time)) {
            JsonUtil.writeError(resp, 409, "You already have a booking for this service at that date and time.");
            return;
        }

        Booking b = new Booking();
        b.setServiceId(service.getId());
        b.setServiceIcon(service.getIcon());
        b.setServiceName(service.getName());
        b.setServicePrice(service.getPrice());
        b.setCustomerName(name.trim());
        b.setPhone(phone.trim());
        b.setLocation(location.trim());
        b.setAddress(address.trim());
        b.setDate(date);
        b.setTime(time);
        b.setIssue(issue);
        b.setIssueSummary(issueSummary);

        int newId = bookingDAO.createBooking(b);
        if (newId == -1) {
            JsonUtil.writeError(resp, 500, "Could not save booking. Please try again.");
            return;
        }
        b.setId(newId);
        b.setStatus("Confirmed");

        // Fire-and-forget style notification; failures here must never break the booking.
        NotificationUtil.sendBookingConfirmation(email, name, service.getName(), date, time);

        JsonUtil.writeJson(resp, 201, b);
    }
}
