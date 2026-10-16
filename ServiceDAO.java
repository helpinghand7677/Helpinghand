package com.helpinghand.dao;

import com.helpinghand.model.ServiceItem;
import com.helpinghand.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceDAO {

    public List<ServiceItem> getAllServices() {
        List<ServiceItem> list = new ArrayList<>();
        String sql = "SELECT * FROM services ORDER BY popular DESC, name ASC";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public ServiceItem getById(String id) {
        String sql = "SELECT * FROM services WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** Used by the admin panel's "Save" button on the pricing tab. */
    public boolean updatePrice(String id, String newPrice) {
        String sql = "UPDATE services SET price = ? WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, newPrice);
            ps.setString(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private ServiceItem mapRow(ResultSet rs) throws SQLException {
        return new ServiceItem(
                rs.getString("id"),
                rs.getString("icon"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getString("price"),
                rs.getString("category"),
                rs.getBoolean("popular")
        );
    }
}
