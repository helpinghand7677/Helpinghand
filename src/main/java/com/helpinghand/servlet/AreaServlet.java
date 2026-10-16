package com.helpinghand.servlet;

import com.helpinghand.dao.AreaDAO;
import com.helpinghand.util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * GET /api/areas -> list of all Patna service areas (id, name, latitude, longitude).
 * Public: used by the booking form dropdown / live-location matching and by
 * the admin technician registration form.
 */
@WebServlet("/api/areas")
public class AreaServlet extends HttpServlet {

    private final AreaDAO areaDAO = new AreaDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        JsonUtil.writeJson(resp, 200, areaDAO.getAllAreas());
    }
}
