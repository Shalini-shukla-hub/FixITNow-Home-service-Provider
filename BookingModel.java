package com.example.signuploginfirebase;

public class BookingModel {
    private String bookingId;
    private String userName;
    private String userId;
    private String serviceType;
    private String location;
    private String selectedDateTime;
    private String serviceProviderName;
    private String serviceProviderId;
    private String userPhone;
    private String issueDescription;
    private String status;

    // Required empty constructor for Firestore
    public BookingModel() {}

    public BookingModel(String bookingId, String userName, String userId, String serviceType,
                        String location, String selectedDateTime, String serviceProviderName,
                        String serviceProviderId, String userPhone, String issueDescription, String status) {
        this.bookingId = bookingId;
        this.userName = userName;
        this.userId = userId;
        this.serviceType = serviceType;
        this.location = location;
        this.selectedDateTime = selectedDateTime;
        this.serviceProviderName = serviceProviderName;
        this.serviceProviderId = serviceProviderId;
        this.userPhone = userPhone;
        this.issueDescription = issueDescription;
        this.status = status;
    }
    private boolean userNotified;

    // Add getter and setter
    public boolean isUserNotified() {
        return userNotified;
    }

    public void setUserNotified(boolean userNotified) {
        this.userNotified = userNotified;
    }

    // Getters
    public String getBookingId() { return bookingId; }
    public String getUserName() { return userName; }
    public String getUserId() { return userId; }
    public String getServiceType() { return serviceType; }
    public String getLocation() { return location; }
    public String getSelectedDateTime() { return selectedDateTime; }
    public String getServiceProviderName() { return serviceProviderName; }
    public String getServiceProviderId() { return serviceProviderId; }
    public String getUserPhone() { return userPhone; }
    public String getIssueDescription() { return issueDescription; }
    public String getStatus() { return status; }

    // Setters
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }
    public void setUserName(String userName) { this.userName = userName; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }
    public void setLocation(String location) { this.location = location; }
    public void setSelectedDateTime(String selectedDateTime) { this.selectedDateTime = selectedDateTime; }
    public void setServiceProviderName(String serviceProviderName) { this.serviceProviderName = serviceProviderName; }
    public void setServiceProviderId(String serviceProviderId) { this.serviceProviderId = serviceProviderId; }
    public void setUserPhone(String userPhone) { this.userPhone = userPhone; }
    public void setIssueDescription(String issueDescription) { this.issueDescription = issueDescription; }
    public void setStatus(String status) { this.status = status; }

    // Aliases for compatibility
    public String getUId() { return userId; }
    public String getName() { return userName; }
}
