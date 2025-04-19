package com.example.signuploginfirebase;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private TextView userEmail;
    private EditText userName, userPhone, userLocation;
    private Button updateBtn, logoutBtn;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        // Find the back button
        ImageView backButton = findViewById(R.id.backButton);

// Handle click event
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, UserDashboardActivity.class);
            startActivity(intent);
            finish(); // Close current activity
        });


        // 🔹 Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // 🔹 Initialize UI Elements
        userEmail = findViewById(R.id.userEmail);
        userName = findViewById(R.id.userName);
        userPhone = findViewById(R.id.userPhone);
        userLocation = findViewById(R.id.userLocation);
        logoutBtn = findViewById(R.id.logoutBtn);

        // 🔹 Fetch and Display User Details
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
            userEmail.setText(currentUser.getEmail()); // Display email
            loadUserProfile(userId); // Fetch and populate profile data
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            redirectToLogin();
        }

        // 🔹 Update Profile Button


        // 🔹 Logout Button
        logoutBtn.setOnClickListener(view -> {
            mAuth.signOut();
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            redirectToLogin();
        });
    }

    private void loadUserProfile(String userId) {
        db.collection("Bookings")
                .whereEqualTo("uId", userId)  // Match the user ID
                .limit(1)  // Get only one document
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                        String name = documentSnapshot.getString("name");
                        String phone = documentSnapshot.getString("phone");
                        String location = documentSnapshot.getString("location");

                        if (name != null) userName.setText(name);
                        if (phone != null) userPhone.setText(phone);
                        if (location != null) userLocation.setText(location);
                    } else {
                        Toast.makeText(this, "User info not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading user info: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("FirestoreError", "Error loading user info", e);
                });
    }


    private void updateUserProfile() {
        String name = userName.getText().toString().trim();
        String phone = userPhone.getText().toString().trim();
        String location = userLocation.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(phone) || TextUtils.isEmpty(location)) {
            Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!phone.matches("\\d{10}")) {
            Toast.makeText(this, "Enter a valid 10-digit phone number!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 🔹 Update data in "user_info"
        Map<String, Object> userUpdates = new HashMap<>();
        userUpdates.put("name", name);
        userUpdates.put("phone", phone);
        userUpdates.put("location", location);

        db.collection("user_bookings").document(userId).update(userUpdates)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error updating profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("FirestoreError", "Error updating profile", e);
                });
    }

    private void redirectToLogin() {
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear backstack
        startActivity(intent);
    }
}