package com.helpinghand.dao;

import com.helpinghand.model.Technician;
import com.helpinghand.util.DBConnection;
import com.helpinghand.util.PasswordUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TechnicianDAO {

    /** Returns the new technician's id, or -1 if the phone is already registered / insert failed. */
    public int register(String name, String phone, String location, String skill, String password) {
        if (findByPhone(phone) != null) return -1;
        String sql = "INSERT INTO technicians (name, phone, location, skill, password_hash) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, phone);
            ps.setString(3, location);
            ps.setString(4, skill);
            ps.setString(5, PasswordUtil.hash(password));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /** Returns the technician if phone+password match, otherwise null. */
    public Technician login(String phone, String password) {
        Technician t = findByPhone(phone);
        if (t != null && PasswordUtil.matches(password, t.getPasswordHash())) return t;
        return null;
    }

    public Technician findByPhone(String phone) {
        String sql = "SELECT * FROM technicians WHERE phone = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, phone);
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
        String sql = "SELECT * FROM technicians ORDER BY created_at DESC, id DESC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
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
        t.setLocation(rs.getString("location"));
        t.setSkill(rs.getString("skill"));
        t.setPasswordHash(rs.getString("password_hash"));
        Timestamp ts = rs.getTimestamp("created_at");
        t.setCreatedAt(ts != null ? ts.toString() : null);
        return t;
    }
}
