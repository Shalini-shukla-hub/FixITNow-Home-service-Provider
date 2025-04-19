package com.example.signuploginfirebase;

import com.google.firebase.Timestamp;

public class AdminServiceProvider {
    private String id;
    private ServiceProvider serviceProvider;
    private boolean verified;
    private boolean adminApproved;
    private Timestamp verificationDate;

    // Constructors
    public AdminServiceProvider() {} // Firestore requirement

    public AdminServiceProvider(ServiceProvider serviceProvider) {
        this.serviceProvider = serviceProvider;
        this.verified = false;
        this.adminApproved = false;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public ServiceProvider getServiceProvider() { return serviceProvider; }
    public void setServiceProvider(ServiceProvider serviceProvider) {
        this.serviceProvider = serviceProvider;
    }

    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }

    public boolean isAdminApproved() { return adminApproved; }
    public void setAdminApproved(boolean adminApproved) {
        this.adminApproved = adminApproved;
    }

    public Timestamp getVerificationDate() { return verificationDate; }
    public void setVerificationDate(Timestamp verificationDate) {
        this.verificationDate = verificationDate;
    }

    // Delegated methods
    public String getName() {
        return serviceProvider != null ? serviceProvider.getName() : "No Name";
    }

    public String getEmail() {
        return serviceProvider != null ? serviceProvider.getEmail() : "No Email";
    }

    public String getPhone() {
        return serviceProvider != null ? serviceProvider.getPhone() : "No Phone";
    }

    public String getServiceType() {
        return serviceProvider != null ? serviceProvider.getServiceType() : "No Type";
    }

    public String getAadharNumber() {
        return serviceProvider != null ? serviceProvider.getAadharNumber() : "";
    }
}