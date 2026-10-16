package com.helpinghand.model;

/**
 * Represents a registered customer of Helping Hand.
 * Admin login is handled separately (see AdminLoginServlet) since the
 * frontend already has a hard-coded demo admin — this class is only
 * for real customer accounts stored in the database.
 */
public class User {
    private int id;
    private String name;
    private String email;
    private String phone;
    private String passwordHash; // never store plain-text passwords
    private String createdAt;

    public User() {}

    public User(int id, String name, String email, String phone, String createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
