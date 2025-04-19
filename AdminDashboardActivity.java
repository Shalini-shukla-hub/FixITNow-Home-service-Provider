package com.example.signuploginfirebase;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AdminDashboardActivity extends AppCompatActivity {
    private Button viewUsersBtn, viewBookingsBtn, verifyProvidersBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // Initialize buttons
        viewUsersBtn = findViewById(R.id.viewUsersBtn);
        viewBookingsBtn = findViewById(R.id.viewBookingsBtn);
        verifyProvidersBtn = findViewById(R.id.verifyProvidersBtn);

        // View Users & Service Providers
        viewUsersBtn.setOnClickListener(v -> {
            Toast.makeText(this, "Viewing Users & Service Providers", Toast.LENGTH_SHORT).show();
            // Navigate to AdminUsersActivity
            Intent intent = new Intent(AdminDashboardActivity.this, AdminUsersActivity.class);
            startActivity(intent);
        });

        // View Bookings
        viewBookingsBtn.setOnClickListener(v -> {
            Toast.makeText(this, "Viewing All Bookings", Toast.LENGTH_SHORT).show();
            // Navigate to AdminBookingsActivity
            Intent intent = new Intent(AdminDashboardActivity.this, AdminBookingsActivity.class);
            startActivity(intent);
        });

        // Verify Service Providers (PAN & Aadhar)
        verifyProvidersBtn.setOnClickListener(v -> {
            Toast.makeText(this, "Verifying Service Providers", Toast.LENGTH_SHORT).show();
            // Navigate to AdminVerificationActivity
            Intent intent = new Intent(AdminDashboardActivity.this, AdminVerificationActivity.class);
            startActivity(intent);
        });
    }
}