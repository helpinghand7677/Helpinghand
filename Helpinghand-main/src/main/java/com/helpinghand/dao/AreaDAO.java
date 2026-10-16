package com.helpinghand.dao;

import com.helpinghand.model.Area;
import com.helpinghand.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AreaDAO {

    public List<Area> getAllAreas() {
        List<Area> list = new ArrayList<>();
        String sql = "SELECT id, name, latitude, longitude FROM areas ORDER BY name ASC";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Area(rs.getInt("id"), rs.getString("name"),
                        rs.getDouble("latitude"), rs.getDouble("longitude")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean exists(int id) {
        String sql = "SELECT 1 FROM areas WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
