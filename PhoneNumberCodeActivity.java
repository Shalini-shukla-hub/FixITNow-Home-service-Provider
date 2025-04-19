package com.example.signuploginfirebase;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class PhoneNumberCodeActivity extends AppCompatActivity {

    EditText otpInput1, otpInput2, otpInput3, otpInput4, otpInput5, otpInput6;
    Button verifyCodeBtn;
    TextView resendOtp; // Added for Resend OTP
    private String userRole; // Holds user role

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_number_code);

        // Initialize views
        otpInput1 = findViewById(R.id.otpInput1);
        otpInput2 = findViewById(R.id.otpInput2);
        otpInput3 = findViewById(R.id.otpInput3);
        otpInput4 = findViewById(R.id.otpInput4);
        otpInput5 = findViewById(R.id.otpInput5);
        otpInput6 = findViewById(R.id.otpInput6);
        verifyCodeBtn = findViewById(R.id.verifyCodeBtn);
        resendOtp = findViewById(R.id.resendOtp); // Initialize Resend OTP TextView

        // Retrieve userRole from the intent
        userRole = getIntent().getStringExtra("userRole");
        Toast.makeText(this, "User Role: " + userRole, Toast.LENGTH_SHORT).show();

        // Setup OTP input fields
        setupOTPInputs();

        // Verify OTP button click listener
        verifyCodeBtn.setOnClickListener(view -> verifyOtp());

        // Resend OTP click listener
        resendOtp.setOnClickListener(view -> onResendOtpClick());
    }

    // Method to verify OTP
    private void verifyOtp() {
        String otp = otpInput1.getText().toString().trim() +
                otpInput2.getText().toString().trim() +
                otpInput3.getText().toString().trim() +
                otpInput4.getText().toString().trim() +
                otpInput5.getText().toString().trim() +
                otpInput6.getText().toString().trim();

        if (otp.length() == 6) {
            Toast.makeText(this, "OTP Verified: " + otp, Toast.LENGTH_SHORT).show();

            // Navigate to UserHomeActivity with the userRole
            Intent intent = new Intent(PhoneNumberCodeActivity.this, UserHomeActivity.class);
            intent.putExtra("userRole", userRole);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Please enter a valid 6-digit OTP", Toast.LENGTH_SHORT).show();
        }
    }

    // OTP field focus movement
    private void setupOTPInputs() {
        EditText[] otpFields = {otpInput1, otpInput2, otpInput3, otpInput4, otpInput5, otpInput6};

        for (int i = 0; i < otpFields.length; i++) {
            final int index = i;
            otpFields[index].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    if (!charSequence.toString().isEmpty() && index < otpFields.length - 1) {
                        otpFields[index + 1].requestFocus();
                    } else if (charSequence.toString().isEmpty() && index > 0) {
                        otpFields[index - 1].requestFocus();
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {}
            });
        }
    }

    // Handle Resend OTP click
    private void onResendOtpClick() {
        Toast.makeText(this, "Resending OTP...", Toast.LENGTH_SHORT).show();

        // TODO: Add actual OTP resend logic (Firebase OTP, SMS API, etc.)
    }
}
