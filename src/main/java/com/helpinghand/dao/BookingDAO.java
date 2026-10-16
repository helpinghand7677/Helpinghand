package com.helpinghand.dao;

import com.helpinghand.model.Booking;
import com.helpinghand.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookingDAO {

    /**
     * Slot-conflict check: is this exact service already booked (and still
     * confirmed) for the same date and time? Prevents two customers from
     * double-booking the same provider slot.
     */
    public boolean isSlotTaken(String serviceId, String date, String time) {
        String sql = "SELECT COUNT(*) FROM bookings " +
                "WHERE service_id = ? AND booking_date = ? AND booking_time = ? AND status = 'Confirmed'";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, serviceId);
            ps.setString(2, date);
            ps.setString(3, time);
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
                "(service_id, service_icon, service_name, service_price, customer_name, phone, address, booking_date, booking_time, issue, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'Confirmed')";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, b.getServiceId());
            ps.setString(2, b.getServiceIcon());
            ps.setString(3, b.getServiceName());
            ps.setString(4, b.getServicePrice());
            ps.setString(5, b.getCustomerName());
            ps.setString(6, b.getPhone());
            ps.setString(7, b.getAddress());
            ps.setString(8, b.getDate());
            ps.setString(9, b.getTime());
            ps.setString(10, b.getIssue());
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
        b.setAddress(rs.getString("address"));
        b.setDate(rs.getString("booking_date"));
        b.setTime(rs.getString("booking_time"));
        b.setIssue(rs.getString("issue"));
        b.setStatus(rs.getString("status"));
        Timestamp ts = rs.getTimestamp("created_at");
        b.setCreatedAt(ts != null ? ts.toString() : null);
        return b;
    }
}
