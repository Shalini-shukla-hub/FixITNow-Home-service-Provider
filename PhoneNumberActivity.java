package com.example.signuploginfirebase;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneNumberActivity extends AppCompatActivity {
    private EditText phoneNumberInput;
    private Button sendCodeBtn;
    private Spinner countryCodeSpinner;
    private FirebaseAuth mAuth;
    private String verificationId;
    private String userRole; // Added to hold user role

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_number);

        // Initialize views
        phoneNumberInput = findViewById(R.id.phoneNumberInput);
        sendCodeBtn = findViewById(R.id.sendCodeBtn);
        countryCodeSpinner = findViewById(R.id.countryCodeSpinner);

        // Initialize Firebase Authentication
        mAuth = FirebaseAuth.getInstance();

        // Retrieve userRole from the Intent
        userRole = getIntent().getStringExtra("userRole");
        Toast.makeText(this, "User Role: " + userRole, Toast.LENGTH_SHORT).show();

        // Populate country code spinner
        String[] countryCodes = {"+1 US", "+91 IN", "+44 UK", "+971 UAE"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, countryCodes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        countryCodeSpinner.setAdapter(adapter);

        // Set click listener for the send code button
        sendCodeBtn.setOnClickListener(view -> {
            String phoneNumber = phoneNumberInput.getText().toString().trim();
            String countryCode = countryCodeSpinner.getSelectedItem().toString().trim().split(" ")[0];

            if (TextUtils.isEmpty(phoneNumber)) {
                Toast.makeText(this, "Please enter a phone number", Toast.LENGTH_SHORT).show();
                return;
            }

            if (phoneNumber.length() < 10) {
                Toast.makeText(this, "Please enter a valid phone number", Toast.LENGTH_SHORT).show();
                return;
            }

            String fullPhoneNumber = countryCode + phoneNumber;
            Toast.makeText(this, "Sending code to " + fullPhoneNumber, Toast.LENGTH_SHORT).show();

            sendVerificationCode(fullPhoneNumber);
        });
    }

    private void sendVerificationCode(String phoneNumber) {
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(phoneNumber) // Phone number to verify
                .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                .setActivity(this) // Activity (for callback binding)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                        signInWithPhoneAuthCredential(credential);
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        Toast.makeText(PhoneNumberActivity.this, "Verification Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                        super.onCodeSent(s, token);
                        verificationId = s;
                        Toast.makeText(PhoneNumberActivity.this, "OTP Sent!", Toast.LENGTH_SHORT).show();

                        // Navigate to the OTP input screen
                        Intent intent = new Intent(PhoneNumberActivity.this, PhoneNumberCodeActivity.class);
                        intent.putExtra("verificationId", verificationId);
                        intent.putExtra("phoneNumber", phoneNumber);
                        intent.putExtra("userRole", userRole); // Pass userRole to OTP activity
                        startActivity(intent);
                    }
                })
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(PhoneNumberActivity.this, "Verification Successful!", Toast.LENGTH_SHORT).show();

                // Navigate to UserHomeActivity with the userRole
                Intent intent = new Intent(PhoneNumberActivity.this, UserHomeActivity.class);
                intent.putExtra("userRole", userRole); // Pass userRole
                startActivity(intent);
                finish();

            } else {
                Toast.makeText(PhoneNumberActivity.this, "Sign-in failed!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
