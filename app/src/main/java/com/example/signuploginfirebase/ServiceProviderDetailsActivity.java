package com.example.signuploginfirebase;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ServiceProviderDetailsActivity extends AppCompatActivity {

    private static final String EXTRA_SERVICE_PROVIDER_NAME = "providerName";
    private static final String EXTRA_SERVICE_TYPE = "providerType";
    private static final String EXTRA_PROVIDER_IMAGE = "providerImage";

    private TextView nameTextView, typeTextView, experienceTextView, descriptionTextView, phoneTextView, selectedDateTimeTextView;
    private ImageView dummyImageView;
    private Button selectDateTimeButton, bookNowButton;
    private FirebaseFirestore db;
    private String selectedDateTime = "";
    private String userLocation = "";
    private String serviceProviderId = "";
    private int providerImageRes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_provider_details);

        // Enable Back Navigation
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Service Provider Details");
        }

        // Initialize Views
        nameTextView = findViewById(R.id.nameTextView);
        typeTextView = findViewById(R.id.typeTextView);
        phoneTextView = findViewById(R.id.phoneTextView);
        experienceTextView = findViewById(R.id.experienceTextView);
        descriptionTextView = findViewById(R.id.descriptionTextView);
        selectedDateTimeTextView = findViewById(R.id.selectedDateTimeTextView);
        dummyImageView = findViewById(R.id.dummyImageView);
        selectDateTimeButton = findViewById(R.id.selectDateTimeButton);
        bookNowButton = findViewById(R.id.bookNowButton);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Retrieve provider details from Intent
        Intent intent = getIntent();
        if (intent != null) {
            String providerName = intent.getStringExtra(EXTRA_SERVICE_PROVIDER_NAME);
            String providerType = intent.getStringExtra(EXTRA_SERVICE_TYPE);
            providerImageRes = intent.getIntExtra(EXTRA_PROVIDER_IMAGE, R.drawable.default_provider_image);

            // Log the values for debugging
            Log.d("ServiceProviderDetails", "Provider Name: " + providerName);
            Log.d("ServiceProviderDetails", "Provider Type: " + providerType);
            Log.d("ServiceProviderDetails", "Provider Image Res: " + providerImageRes);

            // Set provider details and image
            nameTextView.setText(providerName != null ? providerName : "N/A");
            typeTextView.setText(providerType != null ? providerType : "N/A");
            dummyImageView.setImageResource(providerImageRes);

            // Fetch additional details from Firestore
            if (providerName != null) {
                fetchProviderDetails(providerName);
            }
        }

        // Select Date & Time
        selectDateTimeButton.setOnClickListener(v -> showDateTimePicker());

        // Book Service
        bookNowButton.setOnClickListener(v -> bookService());

        // Bottom Navigation Setup
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setItemIconTintList(null);
        bottomNavigationView.setItemTextColor(null);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                startActivity(new Intent(this, UserDashboardActivity.class));
                return true;
            } else if (item.getItemId() == R.id.nav_services) {
                startActivity(new Intent(this, BookingsActivity.class));
                return true;
            } else if (item.getItemId() == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }
            return false;
        });

        // Back Button
        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void fetchProviderDetails(String providerName) {
        db.collection("serviceProviders")
                .whereEqualTo("name", providerName)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                        serviceProviderId = document.getId();

                        // Update UI
                        phoneTextView.setText(document.getString("phone") != null ? document.getString("phone") : "Phone: Not Available");
                        experienceTextView.setText(document.getString("experience") != null ? "Experience: " + document.getString("experience") : "Experience: Not Available");
                        descriptionTextView.setText(document.getString("description") != null ? document.getString("description") : "No description available.");

                        // Check if image URL exists in Firestore
                        String imageUrl = document.getString("imageUrl");
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            // Use Glide to load image from URL with the passed image as placeholder
                            Glide.with(this)
                                    .load(imageUrl)
                                    .placeholder(providerImageRes)
                                    .error(providerImageRes)
                                    .into(dummyImageView);
                        }
                    } else {
                        Log.e("FirestoreData", "No provider found with name: " + providerName);
                        phoneTextView.setText("Phone: Not Available");
                        experienceTextView.setText("Experience: Not Available");
                        descriptionTextView.setText("No description available.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreError", "Error fetching provider details", e);
                    Toast.makeText(this, "Error fetching provider details!", Toast.LENGTH_SHORT).show();
                });
    }

    private void showDateTimePicker() {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this, (datePicker, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String selectedDate = dateFormat.format(calendar.getTime());

            new TimePickerDialog(this, (timePicker, hourOfDay, minute) -> {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                String selectedTime = timeFormat.format(calendar.getTime());
                selectedDateTime = selectedDate + " at " + selectedTime;
                selectedDateTimeTextView.setText("Scheduled at: " + selectedDateTime);
            }, 12, 0, false).show();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void bookService() {
        if (selectedDateTime.isEmpty()) {
            Toast.makeText(this, "Please select a date and time!", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (serviceProviderId.isEmpty()) {
            Toast.makeText(this, "Error: Service Provider ID missing!", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, UserInfoActivity.class);
        intent.putExtra("serviceProviderName", nameTextView.getText().toString());
        intent.putExtra("serviceType", typeTextView.getText().toString());
        intent.putExtra("selectedDateTime", selectedDateTime);
        intent.putExtra("serviceProviderId", serviceProviderId);
        startActivity(intent);
        finish();
    }
}