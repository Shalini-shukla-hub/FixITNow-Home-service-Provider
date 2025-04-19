package com.example.signuploginfirebase;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private TextView userName;
    private Button serviceProviderBtn, userBtn, logoutBtn;
    private GoogleSignInClient gClient;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase instances
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize UI components
        userName = findViewById(R.id.userName);
        serviceProviderBtn = findViewById(R.id.serviceProviderBtn);
        userBtn = findViewById(R.id.userBtn);
        logoutBtn = findViewById(R.id.logout);

        // Set up Google Sign-In options
        GoogleSignInOptions gOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        gClient = GoogleSignIn.getClient(this, gOptions);

        // Display logged-in user's name if available
        GoogleSignInAccount gAccount = GoogleSignIn.getLastSignedInAccount(this);
        if (gAccount != null) {
            userName.setText("Welcome, " + gAccount.getDisplayName() + "!");
        } else {
            userName.setText("");
        }

        // Handle logout button click
        logoutBtn.setOnClickListener(view -> logoutUser());

        // Handle Service Provider button click
        serviceProviderBtn.setOnClickListener(view -> checkUserRole("serviceProvider"));

        // Handle User button click
        userBtn.setOnClickListener(view -> checkUserRole("user"));
    }

    private void logoutUser() {
        gClient.signOut().addOnCompleteListener(task -> {
            mAuth.signOut();
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void checkUserRole(String role) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String collection = role.equals("serviceProvider") ? "serviceProviders" : "users";

        db.collection(collection).document(currentUser.getUid()).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();

                        if (document.exists()) {
                            navigateToDashboard(role);
                        } else {
                            // If not found, register them in Firestore
                            saveUserData(currentUser.getUid(), role);
                            navigateToActivity(PhoneNumberActivity.class, role);
                        }
                    } else {
                        Toast.makeText(this, "Error checking role", Toast.LENGTH_SHORT).show();
                        Log.e("MainActivity", "Error: " + task.getException());
                    }
                });
    }

    private void saveUserData(String userId, String role) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("userId", userId);
        userData.put("isProfileComplete", false);
        userData.put("role", role);

        String collection = role.equals("serviceProvider") ? "serviceProviders" : "users";

        db.collection(collection).document(userId).set(userData)
                .addOnSuccessListener(aVoid -> Log.d("MainActivity", "User data saved"))
                .addOnFailureListener(e -> Log.e("MainActivity", "Error saving data: " + e.getMessage()));
    }

    private void navigateToDashboard(String role) {
        Class<?> activity;
        if (role.equals("serviceProvider")) {
            activity = WelcomeServiceProviderActivity.class;
        } else {
            activity = UserDashboardActivity.class;
        }
        navigateToActivity(activity, role);
    }

    private void navigateToActivity(Class<?> activity, String role) {
        Intent intent = new Intent(MainActivity.this, activity);
        intent.putExtra("userRole", role);
        startActivity(intent);
        Toast.makeText(this, "Navigating to " + role + " dashboard", Toast.LENGTH_SHORT).show();
    }
}