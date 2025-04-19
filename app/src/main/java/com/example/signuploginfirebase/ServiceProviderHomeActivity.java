package com.example.signuploginfirebase;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ServiceProviderHomeActivity extends AppCompatActivity {
    private EditText nameInput, panNumberInput, aadharNumberInput, descriptionInput, phoneInput;
    private Spinner serviceTypeSpinner, experienceSpinner;
    private Button submitBtn;
    private FirebaseFirestore firestore;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_provider_home);

        firestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        nameInput = findViewById(R.id.nameInput);
        serviceTypeSpinner = findViewById(R.id.serviceTypeSpinner);
        experienceSpinner = findViewById(R.id.experienceSpinner);
        panNumberInput = findViewById(R.id.panNumberInput);
        aadharNumberInput = findViewById(R.id.aadharNumberInput);
        descriptionInput = findViewById(R.id.descriptionInput);
        phoneInput = findViewById(R.id.phoneInput); // Phone number input field
        submitBtn = findViewById(R.id.submitBtn);

        // Service Type Dropdown
        String[] services = {"Plumber", "Electrician", "Cleaning", "Painter", "Car Repair", "Carpenter", "Furniture", "TV", "Washing Machine", "AC"};
        ArrayAdapter<String> serviceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, services);
        serviceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        serviceTypeSpinner.setAdapter(serviceAdapter);

        // Experience Dropdown
        String[] experienceLevels = {"Fresher", "1 Year", "2 Years", "3 Years", "More than 3 Years"};
        ArrayAdapter<String> experienceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, experienceLevels);
        experienceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        experienceSpinner.setAdapter(experienceAdapter);

        submitBtn.setOnClickListener(v -> uploadServiceProviderDetails());
    }

    private void uploadServiceProviderDetails() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Authentication error! Please log in again.", Toast.LENGTH_SHORT).show();
            return;
        }

        String name = nameInput.getText().toString().trim();
        String serviceType = serviceTypeSpinner.getSelectedItem().toString();
        String experience = experienceSpinner.getSelectedItem().toString();
        String panNumber = panNumberInput.getText().toString().trim();
        String aadharNumber = aadharNumberInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();
        String phoneNumber = phoneInput.getText().toString().trim();

        // Validation
        if (name.isEmpty() || panNumber.isEmpty() || aadharNumber.isEmpty() || description.isEmpty() || phoneNumber.isEmpty()) {
            Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (panNumber.length() != 10) {
            Toast.makeText(this, "Invalid PAN Number (Must be 10 characters)", Toast.LENGTH_SHORT).show();
            return;
        }

        if (aadharNumber.length() != 12 || !aadharNumber.matches("\\d+")) {
            Toast.makeText(this, "Invalid Aadhar Number (Must be 12 digits)", Toast.LENGTH_SHORT).show();
            return;
        }

        if (phoneNumber.length() != 10 || !phoneNumber.matches("\\d+")) {
            Toast.makeText(this, "Invalid Phone Number (Must be 10 digits)", Toast.LENGTH_SHORT).show();
            return;
        }

        submitBtn.setEnabled(false);

        // Create Firestore Data
        Map<String, Object> serviceProvider = new HashMap<>();
        serviceProvider.put("serviceProviderId", currentUser.getUid()); // Add serviceProviderId field
        serviceProvider.put("uid", currentUser.getUid());
        serviceProvider.put("name", name);
        serviceProvider.put("serviceType", serviceType);
        serviceProvider.put("experience", experience);
        serviceProvider.put("panNumber", panNumber);
        serviceProvider.put("aadharNumber", aadharNumber);
        serviceProvider.put("description", description);
        serviceProvider.put("phone", phoneNumber);
        serviceProvider.put("isProfileComplete", true);

        // Save to Firestore
        firestore.collection("serviceProviders")
                .document(currentUser.getUid())  // UID as Document ID
                .set(serviceProvider)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Service Provider Added: " + name);
                    Toast.makeText(this, "Details Uploaded Successfully!", Toast.LENGTH_SHORT).show();

                    // Navigate to WelcomeServiceProviderActivity
                    Intent intent = new Intent(ServiceProviderHomeActivity.this, WelcomeServiceProviderActivity.class);
                    startActivity(intent);
                    finish();  // Close current activity
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreError", "Error adding provider", e);
                    Toast.makeText(this, "Failed to upload details!", Toast.LENGTH_SHORT).show();
                    submitBtn.setEnabled(true);  // Re-enable submit button on failure
                });
    }
}