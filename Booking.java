package com.example.signuploginfirebase;

public class Booking {
    private String id;
    private String serviceProviderName;
    private String serviceType;
    private String selectedDateTime;
    private String userId;
    private String phone;
    private String status; // New field for booking status

    // Default constructor (required for Firebase)
    public Booking() {}

    // Constructor with parameters
    public Booking(String id, String serviceProviderName, String serviceType, String selectedDateTime, String userId, String phone, String status) {
        this.id = id;
        this.serviceProviderName = serviceProviderName;
        this.serviceType = serviceType;
        this.selectedDateTime = selectedDateTime;
        this.userId = userId;
        this.phone = phone;
        this.status = status;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getServiceProviderName() { return serviceProviderName; }
    public void setServiceProviderName(String serviceProviderName) { this.serviceProviderName = serviceProviderName; }

    public String getServiceType() { return serviceType; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }

    public String getSelectedDateTime() { return selectedDateTime; }
    public void setSelectedDateTime(String selectedDateTime) { this.selectedDateTime = selectedDateTime; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getStatus() { return status; } // Getter for status
    public void setStatus(String status) { this.status = status; } // Setter for status
}
