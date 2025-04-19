package com.example.signuploginfirebase;

public class ServiceProvider {
    private String uid;
    private String name;
    private String serviceType;
    private String experience;  // Experience as a String (e.g., "Fresher", "1 year")
    private String description;
    private String aadharNumber;
    private String phone;
    private String panNumber;
    private boolean isProfileComplete;

    private int imageRes; // New field to store drawable resource ID for the image

    // No-argument constructor (required by Firestore)
    public ServiceProvider() {}

    // Constructor with all fields including imageRes
    public ServiceProvider(String uid, String name, String serviceType, String experience, String description,
                           String aadharNumber, String phone, String panNumber, boolean isProfileComplete,
                           int imageRes) {
        this.uid = uid;
        this.name = name;
        this.serviceType = serviceType;
        this.experience = experience;
        this.description = description;
        this.aadharNumber = aadharNumber;
        this.phone = phone;
        this.panNumber = panNumber;
        this.isProfileComplete = isProfileComplete;
        this.imageRes = imageRes;  // Set the image resource ID here
    }

    // Getters and Setters
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getExperience() {
        return experience != null ? experience : "Experience not available";  // Default message if experience is null
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }

    public String getDescription() {
        return description != null ? description : "Description not available";  // Default message if description is null
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAadharNumber() {
        return aadharNumber;
    }

    public void setAadharNumber(String aadharNumber) {
        this.aadharNumber = aadharNumber;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPanNumber() {
        return panNumber;
    }

    public void setPanNumber(String panNumber) {
        this.panNumber = panNumber;
    }

    public boolean isProfileComplete() {
        return isProfileComplete;
    }

    public void setProfileComplete(boolean profileComplete) {
        isProfileComplete = profileComplete;
    }

    public int getImageRes() {
        return imageRes;
    }

    public void setImageRes(int imageRes) {
        this.imageRes = imageRes;
    }

    public String getEmail() {
        return "";
    }
}
