package com.helpinghand.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Central place that opens a JDBC connection to the MySQL database.
 *
 * Reads connection details from environment variables so the same code
 * works both when running locally in Tomcat and inside Docker:
 *   DB_HOST     (default: localhost)
 *   DB_PORT     (default: 3306)
 *   DB_NAME     (default: helping_hand_db)
 *   DB_USER     (default: root)
 *   DB_PASSWORD (default: your_mysql_password)
 *
 * If you're running outside Docker with no env vars set, just edit the
 * defaults below directly instead.
 */
public class DBConnection {

    private static String env(String key, String fallback) {
        String value = System.getenv(key);
        return (value == null || value.isBlank()) ? fallback : value;
    }

    private static final String DB_HOST = env("DB_HOST", "localhost");
    private static final String DB_PORT = env("DB_PORT", "3306");
    private static final String DB_NAME = env("DB_NAME", "helping_hand_db");
    private static final String DB_USER = env("DB_USER", "root");
    private static final String DB_PASSWORD = env("DB_PASSWORD", "your_mysql_password");

    private static final String URL =
            "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME
                    + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC driver not found on classpath.", e);
        }
        return DriverManager.getConnection(URL, DB_USER, DB_PASSWORD);
    }
}
