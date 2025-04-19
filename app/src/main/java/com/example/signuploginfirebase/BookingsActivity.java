package com.example.signuploginfirebase;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class BookingsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private BookingAdapter bookingAdapter;
    private List<Booking> bookingList;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(BookingsActivity.this, UserDashboardActivity.class);
            startActivity(intent);
            finish(); // Close current activity
        });

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        bookingList = new ArrayList<>();
        bookingAdapter = new BookingAdapter(bookingList);
        recyclerView.setAdapter(bookingAdapter);

        fetchBookings();

        // Setup Bottom Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setItemIconTintList(null);
        bottomNavigationView.setItemTextColor(null);
        setupBottomNavigation(bottomNavigationView);
    }

    private void fetchBookings() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        db.collection("Bookings")
                .whereEqualTo("uId", userId) // Ensure "uId" matches Firestore field
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    bookingList.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Booking booking = document.toObject(Booking.class);
                        if (booking != null) {
                            booking.setId(document.getId());
                            bookingList.add(booking);
                        }
                    }
                    bookingAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load bookings", Toast.LENGTH_SHORT).show()
                );
    }


    private void setupBottomNavigation(BottomNavigationView bottomNavigationView) {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                startActivity(new Intent(BookingsActivity.this, UserDashboardActivity.class));
                finish();
                return true;
            }  if (itemId == R.id.nav_services) {
                return true; // Alread
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(BookingsActivity.this, ProfileActivity.class));
                finish();
                return true;
            }

            return false;
        });
    }


    class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.ViewHolder> {
        private List<Booking> bookings;

        BookingAdapter(List<Booking> bookings) {
            this.bookings = bookings;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_booking, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Booking booking = bookings.get(position);
            holder.nameTextView.setText("Provider: " + booking.getServiceProviderName());
            holder.typeTextView.setText("Service: " + booking.getServiceType());
            holder.dateTextView.setText("Date: " + booking.getSelectedDateTime());

            holder.cancelButton.setOnClickListener(v -> showCancelDialog(booking, position));
        }

        @Override
        public int getItemCount() {
            return bookings.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView nameTextView, typeTextView, dateTextView;
            Button cancelButton;

            ViewHolder(View itemView) {
                super(itemView);
                nameTextView = itemView.findViewById(R.id.nameTextView);
                typeTextView = itemView.findViewById(R.id.typeTextView);
                dateTextView = itemView.findViewById(R.id.dateTextView);
                cancelButton = itemView.findViewById(R.id.cancelButton);
            }
        }

        private void showCancelDialog(Booking booking, int position) {
            new AlertDialog.Builder(BookingsActivity.this)
                    .setTitle("Cancel Booking")
                    .setMessage("Are you sure you want to cancel this booking?")
                    .setPositiveButton("Yes", (dialog, which) -> cancelBooking(booking, position))
                    .setNegativeButton("No", null)
                    .show();
        }

        private void cancelBooking(Booking booking, int position) {
            db.collection("Bookings")
                    .document(booking.getId())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        bookings.remove(position);
                        notifyItemRemoved(position);
                        Toast.makeText(BookingsActivity.this, "Booking cancelled", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(BookingsActivity.this, "Failed to cancel booking", Toast.LENGTH_SHORT).show());
        }
    }
}
