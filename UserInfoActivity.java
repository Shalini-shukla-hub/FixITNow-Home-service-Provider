package com.example.signuploginfirebase;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class UserInfoActivity extends AppCompatActivity {
    private EditText nameEditText, phoneEditText, issueDescriptionEditText, locationEditText;
    private Button confirmBookingButton;
    private FirebaseFirestore db;
    private String serviceProviderName, serviceType, userID;
    private boolean isBookingConfirmed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        // Enable Back Navigation in Toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("User Info");
        }

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Initialize UI elements
        nameEditText = findViewById(R.id.nameEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        issueDescriptionEditText = findViewById(R.id.issueDescriptionEditText);
        locationEditText = findViewById(R.id.locationEditText);
        confirmBookingButton = findViewById(R.id.confirmBookingButton);

        ImageView backButton = findViewById(R.id.backButton);

        // Hide the "Pay Now" button initially


        // Get service provider details from the intent
        if (getIntent() != null) {
            serviceProviderName = getIntent().getStringExtra("serviceProviderName");
            serviceType = getIntent().getStringExtra("serviceType");
        }

        // Fetch saved address (if any)
        fetchSavedAddress();

        // Apply input filters for name and phone fields
        applyInputFilters();

        // Back button click listener
        backButton.setOnClickListener(v -> onBackPressed());

        // Set click listeners for buttons
        confirmBookingButton.setOnClickListener(v -> saveBookingDetails());
      //  payNowButton.setOnClickListener(v -> openDummyPaymentGateway());

        // Setup Bottom Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setItemIconTintList(null);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                startActivity(new Intent(UserInfoActivity.this, UserDashboardActivity.class));
                return true;
            } else if (item.getItemId() == R.id.nav_services) {
                startActivity(new Intent(UserInfoActivity.this, BookingsActivity.class));
                return true;
            } else if (item.getItemId() == R.id.nav_profile) {
                startActivity(new Intent(UserInfoActivity.this, ProfileActivity.class));
                return true;
            }
            return false;
        });
    }

    // Handle Toolbar Back Button

    @Override
    public boolean onSupportNavigateUp() {
        finish(); // Closes the activity and returns to the previous screen
        return true;
    }


    private void fetchSavedAddress() {
        db.collection("user_bookings").document(userID).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.contains("location")) {
                        String savedAddress = documentSnapshot.getString("location");
                        if (savedAddress != null && !savedAddress.isEmpty()) {
                            locationEditText.setText(savedAddress);
                        }
                    } else {
                        locationEditText.setHint("Enter your address");
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to fetch address: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("FirestoreError", "Error fetching address", e);
                });
    }

    private void saveBookingDetails() {
        String name = nameEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String issueDescription = issueDescriptionEditText.getText().toString().trim();
        String userLocation = locationEditText.getText().toString().trim();

        if (name.isEmpty() || phone.isEmpty() || issueDescription.isEmpty() || userLocation.isEmpty()) {
            Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!phone.matches("\\d{10}")) {
            Toast.makeText(this, "Enter a valid 10-digit phone number!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get the selected date and time from the intent
        String selectedDateTime = getIntent().getStringExtra("selectedDateTime");
        String serviceProviderId = getIntent().getStringExtra("serviceProviderId");

        Map<String, Object> bookingData = new HashMap<>();
        bookingData.put("uId", userID);
        bookingData.put("name", name);
        bookingData.put("phone", phone);
        bookingData.put("issueDescription", issueDescription);
        bookingData.put("location", userLocation);
        bookingData.put("serviceProviderName", serviceProviderName);
        bookingData.put("serviceType", serviceType);
        bookingData.put("selectedDateTime", selectedDateTime);
        bookingData.put("serviceProviderId", serviceProviderId);

        // Save the booking to Firestore
        db.collection("Bookings").add(bookingData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Booking saved successfully!", Toast.LENGTH_SHORT).show();
                    onBookingConfirmed();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error saving booking: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("FirestoreError", "Error saving booking", e);
                });
    }

    private void onBookingConfirmed() {
        Toast.makeText(this, "Booking Confirmed!", Toast.LENGTH_LONG).show();
        isBookingConfirmed = true;
        confirmBookingButton.setEnabled(false);

        // Redirect to Home Page after short delay (optional)
        new android.os.Handler().postDelayed(() -> {
            Intent intent = new Intent(UserInfoActivity.this, UserDashboardActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish(); // Finish current activity so user can't go back to it
        }, 1500); // 1.5 seconds delay for better UX (optional)
    }


    private void openDummyPaymentGateway() {
        if (!isBookingConfirmed) {
            Toast.makeText(this, "Please confirm your booking first!", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra("serviceProviderName", serviceProviderName);
        intent.putExtra("serviceType", serviceType);
        intent.putExtra("userName", nameEditText.getText().toString().trim());
        intent.putExtra("userPhone", phoneEditText.getText().toString().trim());
        intent.putExtra("userLocation", locationEditText.getText().toString().trim());
        intent.putExtra("issueDescription", issueDescriptionEditText.getText().toString().trim());
        startActivity(intent);
    }

    private void applyInputFilters() {
        nameEditText.setFilters(new InputFilter[]{onlyLettersFilter});
        phoneEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
        phoneEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().matches("\\d*")) {
                    phoneEditText.setText(s.toString().replaceAll("[^\\d]", ""));
                    phoneEditText.setSelection(phoneEditText.getText().length());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private final InputFilter onlyLettersFilter = (source, start, end, dest, dstart, dend) -> {
        Pattern pattern = Pattern.compile("[a-zA-Z ]*");
        return pattern.matcher(source).matches() ? source : "";
    };
}
