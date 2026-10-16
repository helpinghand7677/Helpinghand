package com.helpinghand.dao;

import com.helpinghand.model.Booking;
import com.helpinghand.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookingDAO {

    /** bookings + the assigned technician's name/phone (LEFT JOIN: unassigned jobs have none). */
    private static final String SELECT_JOINED =
            "SELECT b.*, t.name AS technician_name, t.phone AS technician_phone " +
            "FROM bookings b LEFT JOIN technicians t ON t.id = b.technician_id ";

    /**
     * Slot-conflict check: is this exact service already booked (and still
     * confirmed) for the same date and time? Prevents two customers from
     * double-booking the same provider slot.
     */
    public boolean isSlotTaken(String serviceId, String date, String time) {
        String sql = "SELECT COUNT(*) FROM bookings " +
                "WHERE service_id = ? AND booking_date = ? AND booking_time = ? AND status IN ('Confirmed','Accepted')";
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
                "(service_id, service_icon, service_name, service_price, customer_name, phone, address, area, latitude, longitude, booking_date, booking_time, issue, issue_summary, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'Confirmed')";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, b.getServiceId());
            ps.setString(2, b.getServiceIcon());
            ps.setString(3, b.getServiceName());
            ps.setString(4, b.getServicePrice());
            ps.setString(5, b.getCustomerName());
            ps.setString(6, b.getPhone());
            ps.setString(7, b.getAddress());
            ps.setString(8, b.getArea());
            if (b.getLatitude() != null && b.getLongitude() != null) {
                ps.setDouble(9, b.getLatitude());
                ps.setDouble(10, b.getLongitude());
            } else {
                ps.setNull(9, Types.DECIMAL);
                ps.setNull(10, Types.DECIMAL);
            }
            ps.setString(11, b.getDate());
            ps.setString(12, b.getTime());
            ps.setString(13, b.getIssue());
            ps.setString(14, b.getIssueSummary());
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
        String sql = SELECT_JOINED + "WHERE b.phone = ? ORDER BY b.created_at DESC";
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
        String sql = SELECT_JOINED + "ORDER BY b.created_at DESC";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Admin cancel: works on any booking that is not already Completed/Cancelled. */
    public boolean cancelBooking(int id) {
        String sql = "UPDATE bookings SET status = 'Cancelled' WHERE id = ? AND status IN ('Confirmed','Accepted')";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /** Customer cancel: same, but only if the booking belongs to this phone number. */
    public boolean cancelBookingForPhone(int id, String phone) {
        String sql = "UPDATE bookings SET status = 'Cancelled' " +
                "WHERE id = ? AND phone = ? AND status IN ('Confirmed','Accepted')";
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

    /**
     * Jobs a technician can pick up: still unassigned + Confirmed, same service as the
     * technician's skill, and in the technician's area. Oldest first.
     */
    public List<Booking> getAvailableForTechnician(String serviceId, String areaName) {
        List<Booking> list = new ArrayList<>();
        String sql = SELECT_JOINED +
                "WHERE b.status = 'Confirmed' AND b.technician_id IS NULL " +
                "AND b.service_id = ? AND b.area = ? ORDER BY b.created_at ASC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, serviceId);
            ps.setString(2, areaName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Jobs this technician has accepted (active first, then completed/cancelled). */
    public List<Booking> getByTechnician(int technicianId) {
        List<Booking> list = new ArrayList<>();
        String sql = SELECT_JOINED + "WHERE b.technician_id = ? " +
                "ORDER BY FIELD(b.status,'Accepted','Completed','Cancelled'), b.created_at DESC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, technicianId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Atomic accept: the WHERE clause guarantees only ONE technician can win a job
     * even if two tap "Accept" at the same moment. The service/area match is
     * re-checked here so a technician can't accept someone else's category by
     * hand-crafting a request.
     */
    public boolean acceptJob(int bookingId, int technicianId, String serviceId, String areaName) {
        String sql = "UPDATE bookings SET status = 'Accepted', technician_id = ?, accepted_at = CURRENT_TIMESTAMP " +
                "WHERE id = ? AND status = 'Confirmed' AND technician_id IS NULL " +
                "AND service_id = ? AND area = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, technicianId);
            ps.setInt(2, bookingId);
            ps.setString(3, serviceId);
            ps.setString(4, areaName);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /** Only the technician who accepted a job can mark it completed. */
    public boolean completeJob(int bookingId, int technicianId) {
        String sql = "UPDATE bookings SET status = 'Completed', completed_at = CURRENT_TIMESTAMP " +
                "WHERE id = ? AND technician_id = ? AND status = 'Accepted'";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            ps.setInt(2, technicianId);
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
        b.setArea(rs.getString("area"));
        double lat = rs.getDouble("latitude");
        b.setLatitude(rs.wasNull() ? null : lat);
        double lng = rs.getDouble("longitude");
        b.setLongitude(rs.wasNull() ? null : lng);
        b.setDate(rs.getString("booking_date"));
        b.setTime(rs.getString("booking_time"));
        b.setIssue(rs.getString("issue"));
        b.setIssueSummary(rs.getString("issue_summary"));
        b.setStatus(rs.getString("status"));
        int techId = rs.getInt("technician_id");
        b.setTechnicianId(rs.wasNull() ? null : techId);
        b.setTechnicianName(rs.getString("technician_name"));
        b.setTechnicianPhone(rs.getString("technician_phone"));
        Timestamp acc = rs.getTimestamp("accepted_at");
        b.setAcceptedAt(acc != null ? acc.toString() : null);
        Timestamp done = rs.getTimestamp("completed_at");
        b.setCompletedAt(done != null ? done.toString() : null);
        Timestamp ts = rs.getTimestamp("created_at");
        b.setCreatedAt(ts != null ? ts.toString() : null);
        return b;
    }
}
