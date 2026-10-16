package com.helpinghand.dao;

import com.helpinghand.model.Technician;
import com.helpinghand.util.DBConnection;
import com.helpinghand.util.PasswordUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TechnicianDAO {

    private static final String SELECT_JOINED =
            "SELECT t.id, t.name, t.phone, t.service_id, s.name AS service_name, " +
            "t.area_id, a.name AS area_name, t.created_at " +
            "FROM technicians t " +
            "JOIN services s ON s.id = t.service_id " +
            "JOIN areas a ON a.id = t.area_id ";

    public boolean phoneExists(String phone) {
        String sql = "SELECT 1 FROM technicians WHERE phone = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, phone);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /** Returns the new technician's id, or -1 on failure. */
    public int register(String name, String phone, String password, String serviceId, int areaId) {
        String sql = "INSERT INTO technicians (name, phone, password_hash, service_id, area_id) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, phone);
            ps.setString(3, PasswordUtil.hash(password));
            ps.setString(4, serviceId);
            ps.setInt(5, areaId);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /** Returns the technician if phone + password match, otherwise null. Empty stored hash = no password set yet. */
    public Technician login(String phone, String password) {
        String sql = "SELECT id, password_hash FROM technicians WHERE phone = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, phone);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String hash = rs.getString("password_hash");
                    if (hash != null && !hash.isEmpty() && PasswordUtil.matches(password, hash)) {
                        return getById(rs.getInt("id"));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean setPassword(int id, String password) {
        String sql = "UPDATE technicians SET password_hash = ? WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, PasswordUtil.hash(password));
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Technician getById(int id) {
        String sql = SELECT_JOINED + "WHERE t.id = ?";
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

    public List<Technician> getAll() {
        List<Technician> list = new ArrayList<>();
        String sql = SELECT_JOINED + "ORDER BY t.created_at DESC";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM technicians WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Technician mapRow(ResultSet rs) throws SQLException {
        Technician t = new Technician();
        t.setId(rs.getInt("id"));
        t.setName(rs.getString("name"));
        t.setPhone(rs.getString("phone"));
        t.setServiceId(rs.getString("service_id"));
        t.setServiceName(rs.getString("service_name"));
        t.setAreaId(rs.getInt("area_id"));
        t.setAreaName(rs.getString("area_name"));
        Timestamp ts = rs.getTimestamp("created_at");
        t.setCreatedAt(ts != null ? ts.toString() : null);
        return t;
    }
}
