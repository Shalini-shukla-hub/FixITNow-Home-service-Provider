package com.example.signuploginfirebase;

public class AdminBooking {
    private String userName, serviceProviderName, serviceType, phone;

    public AdminBooking() {
        // Default constructor required for Firestore
    }

    public AdminBooking(String userName, String serviceProviderName, String serviceType, String phone) {
        this.userName = userName;
        this.serviceProviderName = serviceProviderName;
        this.serviceType = serviceType;
        this.phone = phone;
    }

    public String getUserName() {
        return userName;
    }

    public String getServiceProviderName() {
        return serviceProviderName;
    }

    public String getServiceType() {
        return serviceType;
    }

    public String getPhone() {
        return phone;
    }
}
