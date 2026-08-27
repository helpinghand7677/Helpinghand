package com.helpinghand.model;

/**
 * Represents one row from the SERVICES list on the website
 * (AC Repair, Home Cleaning, Plumbing, etc).
 */
public class ServiceItem {
    private String id;      // e.g. "ac-repair-and-service"
    private String icon;    // emoji used on the site
    private String name;
    private String description;
    private String price;   // kept as String because site uses formats like "₹7/sq.ft"
    private String category;
    private boolean popular;

    public ServiceItem() {}

    public ServiceItem(String id, String icon, String name, String description,
                        String price, String category, boolean popular) {
        this.id = id;
        this.icon = icon;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.popular = popular;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public boolean isPopular() { return popular; }
    public void setPopular(boolean popular) { this.popular = popular; }
}
