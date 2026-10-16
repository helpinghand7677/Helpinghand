package com.helpinghand.model;

/**
 * Represents one booking made from the "Book service" form on the site.
 */
public class Booking {
    private int id;
    private String serviceId;
    private String serviceIcon;
    private String serviceName;
    private String servicePrice;
    private String customerName;
    private String phone;
    private String location; // area chosen by the customer, e.g. "Kankarbagh"
    private String address;
    private String date;   // yyyy-mm-dd from the <input type="date">
    private String time;   // HH:mm from the <input type="time">
    private String issue;  // optional notes
    private String issueSummary; // AI-generated summary shown to the technician
    private String status; // "Confirmed", "Accepted" (by a technician) or "Cancelled"
    private String technicianName;
    private String technicianPhone;
    private String createdAt;

    public Booking() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getServiceId() { return serviceId; }
    public void setServiceId(String serviceId) { this.serviceId = serviceId; }

    public String getServiceIcon() { return serviceIcon; }
    public void setServiceIcon(String serviceIcon) { this.serviceIcon = serviceIcon; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public String getServicePrice() { return servicePrice; }
    public void setServicePrice(String servicePrice) { this.servicePrice = servicePrice; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getTechnicianName() { return technicianName; }
    public void setTechnicianName(String technicianName) { this.technicianName = technicianName; }

    public String getTechnicianPhone() { return technicianPhone; }
    public void setTechnicianPhone(String technicianPhone) { this.technicianPhone = technicianPhone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getIssue() { return issue; }
    public void setIssue(String issue) { this.issue = issue; }

    public String getIssueSummary() { return issueSummary; }
    public void setIssueSummary(String issueSummary) { this.issueSummary = issueSummary; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
