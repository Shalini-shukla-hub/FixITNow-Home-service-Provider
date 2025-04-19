package com.example.signuploginfirebase;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class WelcomeServiceProviderActivity extends AppCompatActivity {
    private Button viewBookingsBtn;
    private ImageView profileIcon;
    private TextView welcomeText;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_service_provider);

        // Initialize UI elements
        viewBookingsBtn = findViewById(R.id.viewBookingsBtn);
        profileIcon = findViewById(R.id.profileIcon);
        welcomeText = findViewById(R.id.welcomeText); // The TextView to display the welcome message

        // Initialize Firebase instances
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Load and display the service provider's name
        loadServiceProviderName();

        // Navigate to Booking List
        viewBookingsBtn.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomeServiceProviderActivity.this, ProviderBookingListActivity.class);
            startActivity(intent);
        });

        // Navigate to Profile Page
        profileIcon.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomeServiceProviderActivity.this, ProviderProfileActivity.class);
            startActivity(intent);
        });
    }

    private void loadServiceProviderName() {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            String userId = user.getUid(); // Get current user's UID

            db.collection("serviceProviders") // Ensure this matches your Firestore collection name
                    .document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name"); // Fetch "name" field
                            if (name != null && !name.isEmpty()) {
                                welcomeText.setText("Welcome, " + name + "!");
                            } else {
                                welcomeText.setText("Welcome, Service Provider!");
                            }
                        } else {
                            welcomeText.setText("Welcome, Service Provider!"); // Default text if no data
                        }
                    })
                    .addOnFailureListener(e -> welcomeText.setText("Welcome, Service Provider!")); // Handle errors
        }
    }
}
