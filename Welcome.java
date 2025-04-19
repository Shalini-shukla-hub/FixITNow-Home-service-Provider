package com.example.signuploginfirebase;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class Welcome extends AppCompatActivity {

    private static final int SPLASH_DURATION = 2000; // 3 seconds
    private boolean isNavigated = false; // Prevent double navigation

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // Find the Button by ID
        Button btnSignUp = findViewById(R.id.getStartedButton);

        // Delayed navigation after SPLASH_DURATION
        new Handler().postDelayed(() -> {
            if (!isNavigated) { // Check if already navigated
                navigateToSignUp();
            }
        }, SPLASH_DURATION);

        // Button Click Navigation
        btnSignUp.setOnClickListener(v -> navigateToSignUp());
    }

    private void navigateToSignUp() {
        if (!isNavigated) { // Prevent multiple navigations
            isNavigated = true;
            startActivity(new Intent(Welcome.this, SignUpActivity.class));
            finish();
        }
    }
}
