package com.example.signuploginfirebase;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;

public class AdminBookingsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private AdminBookingsAdapter adapter;
    private List<AdminBooking> adminBookingsList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_bookings);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adminBookingsList = new ArrayList<>();
        adapter = new AdminBookingsAdapter(adminBookingsList);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        fetchBookings();
    }

    private void fetchBookings() {
        CollectionReference bookingsRef = db.collection("user_bookings");

        bookingsRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot value, FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e("Firestore Error", error.getMessage());
                    Toast.makeText(AdminBookingsActivity.this, "Error loading bookings", Toast.LENGTH_SHORT).show();
                    return;
                }

                adminBookingsList.clear(); // Clear list before updating

                for (QueryDocumentSnapshot document : value) {
                    String userName = document.getString("name");
                    String serviceProviderName = document.getString("serviceProviderName");
                    String serviceType = document.getString("serviceType");
                    String phone = document.getString("phone");

                    AdminBooking booking = new AdminBooking(userName, serviceProviderName, serviceType, phone);
                    adminBookingsList.add(booking);
                }

                adapter.notifyDataSetChanged();
            }
        });
    }
}
