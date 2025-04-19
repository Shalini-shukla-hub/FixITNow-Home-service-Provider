package com.example.signuploginfirebase;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;

public class ServiceProviderListActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ServiceProviderListAdapter adapter;
    private List<ServiceProvider> serviceProviderList;
    private FirebaseFirestore firestore;
    private ProgressBar progressBar;
    private String selectedServiceType;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_provider_list);

        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(ServiceProviderListActivity.this, UserDashboardActivity.class);
            startActivity(intent);
            finish(); // Close current activity
        });

        selectedServiceType = getIntent().getStringExtra("serviceType");
        Log.d("Firestore", "Selected Service Type: " + selectedServiceType);

        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setItemIconTintList(null);
        bottomNavigationView.setItemTextColor(null);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        serviceProviderList = new ArrayList<>();
        adapter = new ServiceProviderListAdapter(serviceProviderList, this, provider -> {
            // Pass correct provider image based on service type
            int providerImageRes = getServiceProviderImageResource(provider.getServiceType());

            Intent intent = new Intent(ServiceProviderListActivity.this, ServiceProviderDetailsActivity.class);
            intent.putExtra("providerName", provider.getName());
            intent.putExtra("providerType", provider.getServiceType());
            intent.putExtra("providerImage", providerImageRes);
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);

        firestore = FirebaseFirestore.getInstance();
        fetchServiceProviders();

        setupBottomNavigation();
    }

    private void fetchServiceProviders() {
        if (selectedServiceType == null || selectedServiceType.isEmpty()) {
            Toast.makeText(this, "Invalid Service Type!", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        firestore.collection("serviceProviders")
                .whereEqualTo("serviceType", selectedServiceType.trim())
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful() && task.getResult() != null) {
                        QuerySnapshot snapshot = task.getResult();
                        Log.d("Firestore", "Documents Found: " + snapshot.size());

                        serviceProviderList.clear();
                        for (DocumentSnapshot doc : snapshot.getDocuments()) {
                            ServiceProvider provider = doc.toObject(ServiceProvider.class);
                            if (provider != null) {
                                // 🔥 Assign drawable image resource based on service type
                                int imageRes = getServiceProviderImageResource(provider.getServiceType());
                                provider.setImageRes(imageRes); // 👈 critical fix
                                serviceProviderList.add(provider);
                                Log.d("Firestore", "Provider: " + provider.getName());
                            }
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Log.e("FirestoreError", "Error fetching data", task.getException());
                        Toast.makeText(this, "Failed to fetch data!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                startActivity(new Intent(ServiceProviderListActivity.this, UserDashboardActivity.class));
                return true;
            } else if (itemId == R.id.nav_services) {
                startActivity(new Intent(ServiceProviderListActivity.this, BookingsActivity.class));
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(ServiceProviderListActivity.this, ProfileActivity.class));
                return true;
            }

            return false;
        });
    }

    // This method returns the image resource ID based on service type
    private int getServiceProviderImageResource(String serviceType) {
        if (serviceType == null) return R.drawable.default_provider_image;

        switch (serviceType.toLowerCase()) {
            case "plumber": return R.drawable.plumber_image;    // Ensure these images exist in your drawable folder
            case "electrician": return R.drawable.electrician_image;
            case "cleaning": return R.drawable.cleaner_maitri;
            case "carpenter": return R.drawable.cleaner_maitri;
            case "painter": return R.drawable.painter_aashish;
            case "Car Repair": return R.drawable.plumber_janavi;
            case "TV": return R.drawable.cleaner_pinkesh;
            case "AC": return R.drawable.cleaner_pinkesh;
            case "Washing Machine": return R.drawable.cleaner_pinkesh;
            default: return R.drawable.default_provider_image;  // Default image for unknown types
        }
    }
}
