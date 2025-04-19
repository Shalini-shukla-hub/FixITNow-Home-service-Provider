package com.example.signuploginfirebase;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ProviderBookingListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProviderBookingAdapter adapter;
    private List<BookingModel> bookingList;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1001;
    private static final String CHANNEL_ID = "booking_notifications";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider_booking_list);


        // Initialize UI
        ImageView backArrow = findViewById(R.id.back_arrow);
        backArrow.setOnClickListener(v -> {
            startActivity(new Intent(this, WelcomeServiceProviderActivity.class));
            finish();
        });

        recyclerView = findViewById(R.id.recyclerViewBookings);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Setup adapter
        bookingList = new ArrayList<>();
        adapter = new ProviderBookingAdapter(this, bookingList);
        adapter.setOnCompleteClickListener(this::handleServiceCompletion);
        recyclerView.setAdapter(adapter);

        // Request permissions and load data
        requestNotificationPermissions();
        createNotificationChannel();
        loadProviderBookings();
    }

    private void requestNotificationPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_REQUEST_CODE);
            }
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Booking Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for booking updates");
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
    }

    private void loadProviderBookings() {
        String providerId = auth.getCurrentUser().getUid();

        db.collection("Bookings")
                .whereEqualTo("serviceProviderId", providerId)
                .addSnapshotListener(this::handleBookingUpdates);
    }

    private void handleBookingUpdates(QuerySnapshot snapshots, FirebaseFirestoreException error) {
        if (error != null) {
            Log.e("Firestore", "Error loading bookings", error);
            return;
        }

        List<BookingModel> newBookings = new ArrayList<>();
        for (QueryDocumentSnapshot doc : snapshots) {
            BookingModel booking = new BookingModel(
                    doc.getId(),
                    doc.getString("name"),
                    doc.getString("uId"),
                    doc.getString("serviceType"),
                    doc.getString("location"),
                    doc.getString("selectedDateTime"),
                    doc.getString("serviceProviderName"),
                    doc.getString("serviceProviderId"),
                    doc.getString("phone"),
                    doc.getString("issueDescription"),
                    doc.getString("status") // Add status field
            );
            newBookings.add(booking);
        }

        checkForNewBookings(newBookings);
        bookingList.clear();
        bookingList.addAll(newBookings);
        adapter.notifyDataSetChanged();
    }

    private void completeService(BookingModel booking) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "completed");
        updates.put("userNotified", false); // Reset notification flag

        db.collection("Bookings").document(booking.getBookingId())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    createPaymentRequest(booking);
                    Toast.makeText(this, "Service marked as completed", Toast.LENGTH_SHORT).show();
                });
    }

    private void checkForNewBookings(List<BookingModel> newBookings) {
        for (BookingModel newBooking : newBookings) {
            if (!bookingList.contains(newBooking)) {
                showNewBookingNotification(newBooking);
            }
        }
    }

    private void showNewBookingNotification(BookingModel booking) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("New Booking: " + booking.getServiceType())
                .setContentText("From: " + booking.getName())
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        getSystemService(NotificationManager.class)
                .notify(new Random().nextInt(1000), builder.build());
    }

    private void handleServiceCompletion(BookingModel booking) {
        new
                AlertDialog.Builder(this)
                .setTitle("Confirm Completion")
                .setMessage("Mark this service as completed?")
                .setPositiveButton("Yes", (dialog, which) -> completeService(booking))
                .setNegativeButton("No", null)
                .show();
    }


    private void createPaymentRequest(BookingModel booking) {
        Map<String, Object> payment = new HashMap<>();
        payment.put("userId", booking.getUId());
        payment.put("amount", calculateServiceCharge(booking.getServiceType()));
        payment.put("status", "pending");
        payment.put("timestamp", FieldValue.serverTimestamp());

        db.collection("Payments").document(booking.getBookingId())
                .set(payment)
                .addOnSuccessListener(v -> Log.d("Payment", "Payment request created"));
    }

    private void notifyUser(BookingModel booking) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("title", "Service Completed");
        notification.put("message", "Your " + booking.getServiceType() + " service is done!");
        notification.put("timestamp", FieldValue.serverTimestamp());
        notification.put("type", "service_completed");

        db.collection("users").document(booking.getUId())
                .collection("notifications")
                .add(notification)
                .addOnSuccessListener(ref ->
                        Toast.makeText(this, "User notified", Toast.LENGTH_SHORT).show());
    }

    private int calculateServiceCharge(String serviceType) {
        // Simple pricing logic - replace with your actual pricing
        switch (serviceType.toLowerCase()) {
            case "plumbing": return 500;
            case "electrician": return 600;
            default: return 400;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notifications disabled", Toast.LENGTH_SHORT).show();
            }
        }
    }
}