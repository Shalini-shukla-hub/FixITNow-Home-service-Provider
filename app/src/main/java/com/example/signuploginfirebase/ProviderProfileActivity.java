package com.example.signuploginfirebase;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

public class ProviderProfileActivity extends AppCompatActivity {

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider_profile);

        TextView userName = findViewById(R.id.userName);
        TextView userPhone = findViewById(R.id.userPhone);
        TextView userServiceType = findViewById(R.id.userServiceType);
        Button logoutButton = findViewById(R.id.logoutButton);
        ImageView backArrow = findViewById(R.id.backArrow); // Back arrow added

        db = FirebaseFirestore.getInstance();

        // Fetch the current user's UID
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Fetch provider details from Firestore
        db.collection("serviceProviders").document(uid)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String name = document.getString("name");
                            String phone = document.getString("phone");
                            String serviceType = document.getString("serviceType");

                            // Set the fetched data to TextViews
                            userName.setText(name);
                            userPhone.setText(phone);
                            userServiceType.setText(serviceType);
                        }
                    }
                });

        // Back Arrow Click Listener
        backArrow.setOnClickListener(v -> {
            Intent intent = new Intent(ProviderProfileActivity.this, WelcomeServiceProviderActivity.class);
            startActivity(intent);
            finish(); // Close current activity
        });

        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(ProviderProfileActivity.this, LoginActivity.class));
            finish();
        });
    }
}
