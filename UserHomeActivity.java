package com.example.signuploginfirebase;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class UserHomeActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final String TAG = "UserHomeActivity";

    // UI Components
    private TextView locationText;
    private Button editLocationBtn, nextBtn, getLocationBtn;

    // Firebase
    private FirebaseFirestore firestore;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private FusedLocationProviderClient fusedLocationClient;

    // User data
    private String userRole;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_home);

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Enable Firestore logging for debugging
        FirebaseFirestore.setLoggingEnabled(true);

        // Get current user
        currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        userId = currentUser.getUid();

        // Get user role from intent
        userRole = getIntent().getStringExtra("userRole");
        if (userRole == null) {
            userRole = "user"; // Default role
        }

        // Initialize UI components
        locationText = findViewById(R.id.locationText);
        editLocationBtn = findViewById(R.id.editAddressBtn);
        nextBtn = findViewById(R.id.nextBtn);
        getLocationBtn = findViewById(R.id.getLocationBtn);

        // Load saved address
        loadSavedAddress();

        // Set up button listeners
        setupButtonListeners();
    }

    private void setupButtonListeners() {
        editLocationBtn.setOnClickListener(view -> showManualLocationDialog());

        getLocationBtn.setOnClickListener(view -> {
            if (checkLocationPermission()) {
                getCurrentLocation();
            } else {
                requestLocationPermission();
            }
        });

        nextBtn.setOnClickListener(view -> {
            if (locationText.getText().toString().isEmpty() ||
                    locationText.getText().toString().equals(getString(R.string.no_saved_address))) {
                Toast.makeText(this, "Please save your location first", Toast.LENGTH_SHORT).show();
            } else {
                navigateToDashboard();
            }
        });
    }

    private void loadSavedAddress() {
        String collectionName = getLocationCollectionName();

        firestore.collection(collectionName).document(userId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists() && document.contains("address")) {
                                String savedAddress = document.getString("address");
                                locationText.setText(savedAddress);
                            } else {
                                locationText.setText(getString(R.string.no_saved_address));
                            }
                        } else {
                            Log.e(TAG, "Failed to load address: ", task.getException());
                            Toast.makeText(UserHomeActivity.this,
                                    "Failed to load address: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void showManualLocationDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Enter Your Address");

        final EditText input = new EditText(this);
        input.setHint("Enter your full address");
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newAddress = input.getText().toString().trim();
            if (!newAddress.isEmpty()) {
                saveAddressToFirestore(newAddress);
            } else {
                Toast.makeText(this, "Address cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void saveAddressToFirestore(String address) {
        String collectionName = getLocationCollectionName();

        Map<String, Object> locationData = new HashMap<>();
        locationData.put("address", address);
        locationData.put("userId", userId);
        locationData.put("timestamp", FieldValue.serverTimestamp());

        firestore.collection(collectionName).document(userId)
                .set(locationData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Address saved successfully");
                    Toast.makeText(this, "Address saved successfully", Toast.LENGTH_SHORT).show();
                    locationText.setText(address);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving address: ", e);
                    Toast.makeText(this,
                            "Failed to save address: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private boolean checkLocationPermission() {
        return ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(this,
                        "Location permission denied. Cannot fetch location.",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getCurrentLocation() {
        if (checkLocationPermission()) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            String latLng = "Lat: " + location.getLatitude() +
                                    ", Lng: " + location.getLongitude();
                            saveAddressToFirestore(latLng);
                        } else {
                            Toast.makeText(this,
                                    "Unable to get current location. Try again.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error getting location: ", e);
                        Toast.makeText(this,
                                "Error getting location: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private String getLocationCollectionName() {
        return "serviceProvider".equals(userRole) ? "providerlocation" : "userlocation";
    }

    private void navigateToDashboard() {
        Intent intent;
        if ("serviceProvider".equals(userRole)) {
            intent = new Intent(this, ServiceProviderHomeActivity.class);
        } else {
            intent = new Intent(this, UserDashboardActivity.class);
        }
        intent.putExtra("userRole", userRole);
        startActivity(intent);
        finish();
    }
}