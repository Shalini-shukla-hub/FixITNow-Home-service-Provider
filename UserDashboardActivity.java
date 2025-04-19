package com.example.signuploginfirebase;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class UserDashboardActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SearchView searchView;
    private ServiceProviderAdapter adapter;
    private List<String> categoryList, filteredList;
    private FirebaseFirestore db;
    private BottomNavigationView bottomNavigationView;
    private ListenerRegistration bookingListener;
    private static final int NOTIFICATION_PERMISSION_CODE = 1001;
    private static final String CHANNEL_ID = "service_channel";
    @Override
    protected void onStart() {
        super.onStart();
        checkForCompletedServices();
    }

    private void checkForCompletedServices() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (userId == null) return;

        db.collection("Bookings")
                .whereEqualTo("uId", userId)
                .whereEqualTo("status", "completed")
                .whereEqualTo("userNotified", false)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        // Show popup for each completed service
                        showServiceCompletedPopup(document.getId(), document.getString("serviceType"));

                        // Mark as notified
                        document.getReference().update("userNotified", true);
                    }
                });
    }

    private void showServiceCompletedPopup(String bookingId, String serviceType) {
        new AlertDialog.Builder(this)
                .setTitle("Service Completed!")
                .setMessage("Your " + serviceType + " service has been completed successfully!")
                .setPositiveButton("Proceed to Payment", (dialog, which) -> {
                    // Launch payment activity
                    Intent intent = new Intent(this, PaymentActivity.class);
                    intent.putExtra("bookingId", bookingId);
                    startActivity(intent);
                })
                .setNegativeButton("Later", null)
                .setCancelable(false)
                .show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_dashboard);

        initializeUI();
        setupFirestore();
        setupNotifications();
        loadServiceCategories();
        setupNavigation();
        setupSearch();
        setupBookingStatusListener();
        setupNotificationListener();
    }

    private void initializeUI() {
        recyclerView = findViewById(R.id.recyclerView);
        searchView = findViewById(R.id.searchView);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(null);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(50, 20));

        categoryList = new ArrayList<>();
        filteredList = new ArrayList<>();
        adapter = new ServiceProviderAdapter(this, filteredList);
        recyclerView.setAdapter(adapter);
    }

    private void setupFirestore() {
        db = FirebaseFirestore.getInstance();
    }

    private void setupNotifications() {
        createNotificationChannel();
        requestNotificationPermission();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Service Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for service updates");
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_CODE);
            }
        }
    }

    private void loadServiceCategories() {
        db.collection("serviceProviders")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (isFinishing()) return;
                    Set<String> uniqueCategories = new HashSet<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String category = document.getString("serviceType");
                        if (category != null && !category.trim().isEmpty()) {
                            uniqueCategories.add(category.trim());
                        }
                    }
                    categoryList.clear();
                    categoryList.addAll(uniqueCategories);
                    updateFilteredList();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load data: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void setupNavigation() {
        bottomNavigationView.setOnNavigationItemSelectedListener(this::onNavigationItemSelected);
        bottomNavigationView.setItemIconTintList(null);
        bottomNavigationView.setItemTextColor(null);
    }

    private void setupSearch() {
        searchView.setIconifiedByDefault(false);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) { return false; }
            @Override public boolean onQueryTextChange(String newText) {
                filterList(newText);
                return true;
            }
        });
    }

    private void setupBookingStatusListener() {
        String currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        if (currentUserEmail == null) return;

        bookingListener = db.collection("Bookings")
                .whereEqualTo("userEmail", currentUserEmail)
                .whereEqualTo("status", "Completed")
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        Log.e("Firestore", "Listen failed", error);
                        return;
                    }

                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        for (DocumentChange dc : querySnapshot.getDocumentChanges()) {
                            if (dc.getType() == DocumentChange.Type.ADDED ||
                                    dc.getType() == DocumentChange.Type.MODIFIED) {

                                QueryDocumentSnapshot document = dc.getDocument();
                                String bookingId = document.getId();
                                showPaymentNotification(bookingId);
                                launchPaymentActivity(bookingId);
                            }
                        }
                    }
                });
    }

    private void showPaymentNotification(String bookingId) {
        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra("bookingId", bookingId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Service Completed!")
                .setContentText("Tap to complete payment")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.notify(new Random().nextInt(1000), builder.build());
    }

    private void launchPaymentActivity(String bookingId) {
        Toast.makeText(this, "Service Completed! Tap to pay.", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra("bookingId", bookingId);
        startActivity(intent);
    }

    private void setupNotificationListener() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("users").document(userId)
                .collection("notifications")
                .whereEqualTo("read", false)
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null || querySnapshot == null) return;

                    for (DocumentChange dc : querySnapshot.getDocumentChanges()) {
                        if (dc.getType() == DocumentChange.Type.ADDED) {
                            showNotification(
                                    dc.getDocument().getString("title"),
                                    dc.getDocument().getString("message")
                            );
                            dc.getDocument().getReference().update("read", true);
                        }
                    }
                });
    }

    private void showNotification(String title, String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.notify(new Random().nextInt(1000), builder.build());
    }

    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Class<?> targetActivity = null;

        if (item.getItemId() == R.id.nav_home) {
            return true;
        } else if (item.getItemId() == R.id.nav_services) {
            targetActivity = BookingsActivity.class;
        } else if (item.getItemId() == R.id.nav_profile) {
            targetActivity = ProfileActivity.class;
        }

        if (targetActivity != null) {
            startActivity(new Intent(this, targetActivity));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
        return true;
    }

    private void filterList(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            updateFilteredList();
        } else {
            String lowerQuery = query.toLowerCase().trim();
            for (String category : categoryList) {
                if (category.toLowerCase().contains(lowerQuery)) {
                    filteredList.add(category);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void updateFilteredList() {
        filteredList.clear();
        filteredList.addAll(categoryList);
        adapter.notifyDataSetChanged();
    }

    private static class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {
        private final int leftSpacing, spacing;

        public GridSpacingItemDecoration(int leftSpacing, int spacing) {
            this.leftSpacing = leftSpacing;
            this.spacing = spacing;
        }

        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                                   @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view);
            outRect.left = (position % 2 == 0) ? leftSpacing : spacing;
            outRect.right = spacing;
            outRect.bottom = spacing;
            if (position < 2) outRect.top = spacing;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bookingListener != null) {
            bookingListener.remove();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notifications disabled", Toast.LENGTH_SHORT).show();
            }
        }
    }

}