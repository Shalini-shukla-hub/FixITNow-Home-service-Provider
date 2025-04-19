package com.example.signuploginfirebase;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminVerificationActivity extends AppCompatActivity {

    private static final String TAG = "AdminVerification";
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private AdminServiceProviderAdapter adapter;
    private List<AdminServiceProvider> adminServiceProviders;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_verification);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize views
        recyclerView = findViewById(R.id.recyclerViewServiceProviders);
        progressBar = findViewById(R.id.progressBar);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adminServiceProviders = new ArrayList<>();
        adapter = new AdminServiceProviderAdapter(adminServiceProviders, this::handleVerification);
        recyclerView.setAdapter(adapter);

        // First initialize the adminApproved field if needed
        initializeAdminApprovedField();
    }

    private void initializeAdminApprovedField() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("serviceProviders")
                .whereEqualTo("isProfileComplete", true)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> docsToUpdate = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            if (!document.contains("adminApproved")) {
                                docsToUpdate.add(document.getId());
                            }
                        }

                        if (docsToUpdate.isEmpty()) {
                            loadServiceProvidersForVerification();
                            return;
                        }

                        // Batch update all documents missing the field
                        Map<String, Object> updateData = new HashMap<>();
                        updateData.put("adminApproved", false);
                        updateData.put("verified", false);
                        updateData.put("lastUpdated", FieldValue.serverTimestamp());

                        int total = docsToUpdate.size();
                        for (String docId : docsToUpdate) {
                            db.collection("serviceProviders")
                                    .document(docId)
                                    .update(updateData)
                                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Updated "+docId))
                                    .addOnFailureListener(e -> Log.e(TAG, "Error updating "+docId, e));
                        }

                        Toast.makeText(this,
                                "Initialized "+total+" service provider records",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e(TAG, "Error checking documents", task.getException());
                    }
                    loadServiceProvidersForVerification();
                });
    }

    private void loadServiceProvidersForVerification() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("serviceProviders")
                .whereEqualTo("isProfileComplete", true)
                .whereEqualTo("adminApproved", false)
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        adminServiceProviders.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                ServiceProvider provider = document.toObject(ServiceProvider.class);
                                AdminServiceProvider adminProvider = new AdminServiceProvider(provider);
                                adminProvider.setId(document.getId());
                                adminServiceProviders.add(adminProvider);
                            } catch (Exception e) {
                                Log.e(TAG, "Error mapping document "+document.getId(), e);
                            }
                        }

                        if (adminServiceProviders.isEmpty()) {
                            Toast.makeText(this,
                                    "All service providers are verified",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            adapter.notifyDataSetChanged();
                        }
                    } else {
                        Toast.makeText(this,
                                "Error loading providers: "+task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error loading providers", task.getException());
                    }
                });
    }

    private void handleVerification(AdminServiceProvider provider, boolean approved) {
        progressBar.setVisibility(View.VISIBLE);
        Map<String, Object> updates = new HashMap<>();
        updates.put("adminApproved", approved);
        updates.put("verified", approved);
        updates.put("lastUpdated", FieldValue.serverTimestamp());

        db.collection("serviceProviders")
                .document(provider.getId())
                .update(updates)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        adminServiceProviders.remove(provider);
                        adapter.notifyDataSetChanged();
                        Toast.makeText(this,
                                approved ? "Approved successfully" : "Rejected successfully",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this,
                                "Operation failed: "+task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Verification failed", task.getException());
                    }
                });
    }
}