package com.example.signuploginfirebase;

public class AdminUser {
    private String name;
    private String serviceType;
    private String userName;

    public AdminUser(String name, String serviceType, String userName) {
        this.name = name;
        this.serviceType = serviceType;
        this.userName = userName;
    }

    public String getName() {
        return name;
    }

    public String getServiceType() {
        return serviceType;
    }

    public String getUserName() {
        return userName;
    }
}
