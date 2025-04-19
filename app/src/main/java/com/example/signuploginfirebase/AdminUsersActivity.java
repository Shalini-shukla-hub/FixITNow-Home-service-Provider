package com.example.signuploginfirebase;

import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class AdminUsersActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AdminUsersAdapter adapter;
    private List<AdminUser> userList;
    private FirebaseFirestore firestore;
    private List<String> userNames; // To store user names

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_user_display);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        userList = new ArrayList<>();
        adapter = new AdminUsersAdapter(this, userList);
        recyclerView.setAdapter(adapter);

        firestore = FirebaseFirestore.getInstance();
        userNames = new ArrayList<>();

        fetchUsers(); // Fetch user bookings first
    }

    private void fetchUsers() {
        firestore.collection("Bookings")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot doc : task.getResult()) {
                            String userName = doc.getString("name");
                            if (userName != null) {
                                userNames.add(userName); // Store usernames
                            }
                        }
                        fetchServiceProviders(); // Fetch service providers after users
                    } else {
                        Toast.makeText(this, "Failed to load users", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchServiceProviders() {
        firestore.collection("serviceProviders")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int userIndex = 0; // Track user index
                        for (DocumentSnapshot doc : task.getResult()) {
                            String providerName = doc.getString("name");
                            String serviceType = doc.getString("serviceType");

                            String userName = userIndex < userNames.size() ? userNames.get(userIndex) : "-"; // Assign username if available
                            userIndex++; // Move to next user

                            if (providerName != null && serviceType != null) {
                                userList.add(new AdminUser(providerName, serviceType, userName));
                            }
                        }
                        adapter.notifyDataSetChanged(); // Update list
                    } else {
                        Toast.makeText(this, "Failed to load service providers", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
