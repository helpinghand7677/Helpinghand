package com.helpinghand.dao;

import com.helpinghand.model.Booking;
import com.helpinghand.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookingDAO {

    /**
     * Duplicate check: has this same customer (phone) already got an active booking for
     * the same service, date and time slot? (Several technicians exist, so different
     * customers may of course book the same slot.)
     */
    public boolean isDuplicate(String phone, String serviceId, String date, String time) {
        String sql = "SELECT COUNT(*) FROM bookings " +
                "WHERE phone = ? AND service_id = ? AND booking_date = ? AND booking_time = ? AND status IN ('Confirmed', 'Accepted')";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, phone);
            ps.setString(2, serviceId);
            ps.setString(3, date);
            ps.setString(4, time);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /** Inserts a new booking (status is always "Confirmed" at creation time). Returns new id, or -1 on failure. */
    public int createBooking(Booking b) {
        String sql = "INSERT INTO bookings " +
                "(service_id, service_icon, service_name, service_price, customer_name, phone, location, address, booking_date, booking_time, issue, issue_summary, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'Confirmed')";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, b.getServiceId());
            ps.setString(2, b.getServiceIcon());
            ps.setString(3, b.getServiceName());
            ps.setString(4, b.getServicePrice());
            ps.setString(5, b.getCustomerName());
            ps.setString(6, b.getPhone());
            ps.setString(7, b.getLocation());
            ps.setString(8, b.getAddress());
            ps.setString(9, b.getDate());
            ps.setString(10, b.getTime());
            ps.setString(11, b.getIssue());
            ps.setString(12, b.getIssueSummary());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /** "My Bookings" for a given phone number (the site does not require login for booking). */
    public List<Booking> getBookingsByPhone(String phone) {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT * FROM bookings WHERE phone = ? ORDER BY created_at DESC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, phone);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Full list, used by the admin panel. */
    public List<Booking> getAllBookings() {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT * FROM bookings ORDER BY created_at DESC";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** New (not yet accepted) orders in a technician's area for their skill. */
    public List<Booking> getAvailableForTechnician(String skill, String location) {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT * FROM bookings WHERE status = 'Confirmed' AND service_name = ? AND location = ? " +
                "ORDER BY booking_date, created_at";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, skill);
            ps.setString(2, location);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Orders this technician has already accepted. */
    public List<Booking> getAcceptedByTechnician(String technicianPhone) {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT * FROM bookings WHERE status = 'Accepted' AND technician_phone = ? ORDER BY booking_date, created_at";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, technicianPhone);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Accepts a booking for a technician. The WHERE clause makes it atomic: if two
     * technicians tap Accept at the same time only the first UPDATE matches a
     * 'Confirmed' row, the second gets false.
     */
    public boolean acceptBooking(int id, String technicianName, String technicianPhone) {
        String sql = "UPDATE bookings SET status = 'Accepted', technician_name = ?, technician_phone = ? " +
                "WHERE id = ? AND status = 'Confirmed'";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, technicianName);
            ps.setString(2, technicianPhone);
            ps.setInt(3, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /** Looks up one booking (used to check skill/location before a technician accepts it). */
    public Booking getById(int id) {
        String sql = "SELECT * FROM bookings WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** Customer-side cancel: only works if the phone number matches the one on the booking. */
    public boolean cancelBookingForPhone(int id, String phone) {
        String sql = "UPDATE bookings SET status = 'Cancelled' WHERE id = ? AND phone = ? AND status <> 'Cancelled'";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setString(2, phone);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean cancelBooking(int id) {
        String sql = "UPDATE bookings SET status = 'Cancelled' WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Booking mapRow(ResultSet rs) throws SQLException {
        Booking b = new Booking();
        b.setId(rs.getInt("id"));
        b.setServiceId(rs.getString("service_id"));
        b.setServiceIcon(rs.getString("service_icon"));
        b.setServiceName(rs.getString("service_name"));
        b.setServicePrice(rs.getString("service_price"));
        b.setCustomerName(rs.getString("customer_name"));
        b.setPhone(rs.getString("phone"));
        b.setLocation(rs.getString("location"));
        b.setAddress(rs.getString("address"));
        b.setDate(rs.getString("booking_date"));
        b.setTime(rs.getString("booking_time"));
        b.setIssue(rs.getString("issue"));
        b.setIssueSummary(rs.getString("issue_summary"));
        b.setStatus(rs.getString("status"));
        b.setTechnicianName(rs.getString("technician_name"));
        b.setTechnicianPhone(rs.getString("technician_phone"));
        Timestamp ts = rs.getTimestamp("created_at");
        b.setCreatedAt(ts != null ? ts.toString() : null);
        return b;
    }
}
