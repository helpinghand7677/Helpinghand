package com.helpinghand.util;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Runs when the app starts: creates all tables (and seeds the service catalog)
 * in whatever database DB_NAME points to, so a fresh cloud MySQL (Aiven etc.)
 * needs no manual setup. Everything is idempotent - safe to run on every start.
 * Also adds any columns that older versions of the tables are missing.
 */
@WebListener
public class SchemaInitializer implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try (Connection con = DBConnection.getConnection()) {
            runScript(con);
            ensureColumn(con, "bookings", "location", "VARCHAR(100) NOT NULL DEFAULT ''");
            ensureColumn(con, "bookings", "issue_summary", "VARCHAR(1000)");
            ensureColumn(con, "bookings", "technician_name", "VARCHAR(120)");
            ensureColumn(con, "bookings", "technician_phone", "VARCHAR(20)");
            System.out.println("[HelpingHand] Database schema is ready.");
        } catch (Exception e) {
            System.err.println("[HelpingHand] Could not initialise the database - check DB_* environment variables:");
            e.printStackTrace();
        }
    }

    private void runScript(Connection con) throws Exception {
        StringBuilder sql = new StringBuilder();
        try (InputStream in = SchemaInitializer.class.getResourceAsStream("/schema.sql")) {
            if (in == null) throw new IllegalStateException("schema.sql not found on classpath");
            try (BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                while ((line = r.readLine()) != null) {
                    if (line.trim().startsWith("--")) continue; // comment line
                    sql.append(line).append('\n');
                    if (line.trim().endsWith(";")) {
                        String stmt = sql.toString().trim();
                        stmt = stmt.substring(0, stmt.length() - 1);
                        sql.setLength(0);
                        if (stmt.isBlank()) continue;
                        try (Statement st = con.createStatement()) {
                            st.execute(stmt);
                        }
                    }
                }
            }
        }
    }

    private void ensureColumn(Connection con, String table, String column, String definition) throws Exception {
        DatabaseMetaData meta = con.getMetaData();
        try (ResultSet rs = meta.getColumns(con.getCatalog(), null, table, column)) {
            if (rs.next()) return;
        }
        try (Statement st = con.createStatement()) {
            st.execute("ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition);
        }
    }
}
