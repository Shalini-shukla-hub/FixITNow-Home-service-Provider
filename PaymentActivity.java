package com.example.signuploginfirebase;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class PaymentActivity extends AppCompatActivity {

    private TextView paymentStatus;
    private Button paySuccessButton, payFailureButton;
    private BottomNavigationView bottomNavigationView;
    private FirebaseFirestore db;
    private String bookingId;
    private int selectedRating = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Get booking ID from intent
        bookingId = getIntent().getStringExtra("bookingId");
        if (bookingId == null) {
            Toast.makeText(this, "Invalid booking reference", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        setupBottomNavigation();
        setupPaymentButtons();
    }

    private void initializeViews() {
        paymentStatus = findViewById(R.id.paymentStatus);
        paySuccessButton = findViewById(R.id.paySuccessButton);
        payFailureButton = findViewById(R.id.payFailureButton);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setItemIconTintList(null);
        bottomNavigationView.setOnItemSelectedListener(this::onNavigationItemSelected);
    }

    private void setupPaymentButtons() {
        paySuccessButton.setOnClickListener(v -> processPayment(true));
        payFailureButton.setOnClickListener(v -> processPayment(false));
    }

    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        Intent intent = null;

        if (id == R.id.nav_home) {
            intent = new Intent(this, UserDashboardActivity.class);
        } else if (id == R.id.nav_services) {
            intent = new Intent(this, BookingsActivity.class);
        } else if (id == R.id.nav_profile) {
            intent = new Intent(this, ProfileActivity.class);
        }

        if (intent != null) {
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
            return true;
        }
        return false;
    }

    private void processPayment(boolean isSuccess) {
        if (isSuccess) {
            paymentStatus.setText("Payment Successful!");
            Toast.makeText(this, "Payment completed successfully!", Toast.LENGTH_LONG).show();

            // Update payment status in Firestore
            db.collection("Payments").document(bookingId)
                    .update("status", "paid")
                    .addOnSuccessListener(aVoid -> showFeedbackDialog())
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Failed to update payment status", Toast.LENGTH_SHORT).show());
        } else {
            paymentStatus.setText("Payment Failed!");
            Toast.makeText(this, "Payment failed. Try again.", Toast.LENGTH_LONG).show();
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    private void showFeedbackDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_feedback, null);
        TextInputEditText feedbackInput = dialogView.findViewById(R.id.feedbackInput);

        // Initialize star views
        ImageView[] stars = new ImageView[]{
                dialogView.findViewById(R.id.star1),
                dialogView.findViewById(R.id.star2),
                dialogView.findViewById(R.id.star3),
                dialogView.findViewById(R.id.star4),
                dialogView.findViewById(R.id.star5)
        };

        // Set up star click listeners
        for (int i = 0; i < stars.length; i++) {
            final int rating = i + 1;
            stars[i].setOnClickListener(v -> {
                selectedRating = rating;
                updateStars(stars, selectedRating);
            });
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle("How was your service?")
                .setView(dialogView)
                .setPositiveButton("Submit", (dialog, which) -> {
                    String feedback = feedbackInput.getText().toString().trim();
                    if (selectedRating > 0) {
                        saveFeedbackToFirestore(feedback, selectedRating);
                    } else {
                        Toast.makeText(this, "Please select a rating", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Skip", (dialog, which) -> finishPaymentProcess())
                .setCancelable(false)
                .show();
    }

    private void updateStars(ImageView[] stars, int rating) {
        for (int i = 0; i < stars.length; i++) {
            stars[i].setImageResource(i < rating ? R.drawable.ic_star_filled : R.drawable.ic_star_outline);
        }
    }

    private void saveFeedbackToFirestore(String feedback, int rating) {
        Map<String, Object> reviewData = new HashMap<>();
        reviewData.put("feedback", feedback);
        reviewData.put("rating", rating);
        reviewData.put("timestamp", FieldValue.serverTimestamp());
        reviewData.put("bookingId", bookingId);

        db.collection("Reviews")
                .document(bookingId)
                .set(reviewData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Thank you for your feedback!", Toast.LENGTH_SHORT).show();
                    finishPaymentProcess();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save feedback", Toast.LENGTH_SHORT).show();
                    finishPaymentProcess();
                });
    }


    private void finishPaymentProcess() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("paymentStatus", "success");
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}