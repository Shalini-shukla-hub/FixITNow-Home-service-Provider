package com.example.signuploginfirebase;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ServiceProviderBookingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_provider_bookings);

        // Set "Hello, Users!" text
        TextView textView = findViewById(R.id.textViewHello);
        textView.setText("Hello, Users!");
    }
}
