package com.example.signuploginfirebase;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.developer.gbuttons.GoogleSignInButton;
import com.google.android.gms.auth.
        api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {

    private EditText loginEmail, loginPassword;
    private TextView signupRedirectText, forgotPassword;
    private Button loginButton;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private GoogleSignInButton googleBtn;
    private GoogleSignInClient gClient;
    private Toast toast;
    private ImageView passwordToggle;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginEmail = findViewById(R.id.login_email);
        loginPassword = findViewById(R.id.login_password);
        loginButton = findViewById(R.id.login_button);
        signupRedirectText = findViewById(R.id.signUpRedirectText);
        forgotPassword = findViewById(R.id.forgot_password);
        googleBtn = findViewById(R.id.googleBtn);
        passwordToggle = findViewById(R.id.password_toggle);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance(); // Initialize Firestore

        loginButton.setOnClickListener(v -> loginUser());
        signupRedirectText.setOnClickListener(v -> startActivity(new Intent(this, SignUpActivity.class)));
        forgotPassword.setOnClickListener(v -> showForgotPasswordDialog());
        setupGoogleSignIn();
        passwordToggle.setOnClickListener(v -> togglePasswordVisibility());
    }

    private void showForgotPasswordDialog() {
        // Implement password reset functionality if needed
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            loginPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            passwordToggle.setImageResource(R.drawable.ic_eyeopen);
        } else {
            loginPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            passwordToggle.setImageResource(R.drawable.ic_eyeopen);
        }
        loginPassword.setSelection(loginPassword.getText().length());
        isPasswordVisible = !isPasswordVisible;
    }

    private void loginUser() {
        String email = loginEmail.getText().toString().trim().toLowerCase();
        String password = loginPassword.getText().toString().trim();

        if (!isValidEmail(email) || !isValidPassword(password)) {
            return;
        }

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            Log.d("AuthDebug", "User authenticated: " + user.getEmail());
                            checkUserFirestore(user.getUid(), user.getEmail());
                        }
                    } else {
                        handleLoginError(task.getException());
                    }
                });
    }

    private void checkUserFirestore(String userId, String email) {
        db.collection("app_users")
                .whereEqualTo("email", email)
                .limit(1) // Prevent multiple matches
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                        String role = document.getString("role");
                        Log.d("LoginDebug", "User role: " + role);

                        if (role != null) {
                            if (role.equalsIgnoreCase("admin")) {
                                navigateToAdminDashboard();
                            } else if (role.equalsIgnoreCase("user")) {
                                navigateToMainActivity(); // Navigate to user home page
                            } else {
                                Log.d("LoginDebug", "Unknown role: " + role);
                                showToast("Invalid user role. Contact support.");
                            }
                        } else {
                            Log.d("LoginDebug", "Role field is missing in Firestore");
                            showToast("User role not found. Contact support.");
                        }
                    } else {
                        Log.d("FirestoreDebug", "No user found or error fetching user", task.getException());
                        showToast("User data not found. Please sign up.");
                    }
                });
    }

    private boolean isValidEmail(String email) {
        if (TextUtils.isEmpty(email)) {
            loginEmail.setError("Email is required");
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            loginEmail.setError("Enter a valid email address");
            return false;
        }
        return true;
    }

    private boolean isValidPassword(String password) {
        Pattern specialCharPattern = Pattern.compile("[!@#$%^&*()\\-+=]");
        Pattern digitPattern = Pattern.compile("[0-9]");

        if (TextUtils.isEmpty(password)) {
            loginPassword.setError("Password is required");
            return false;
        }
        if (password.length() < 8) {
            loginPassword.setError("Password must be at least 8 characters");
            return false;
        }
        if (!specialCharPattern.matcher(password).find()) {
            loginPassword.setError("Include at least one special character (!@#$%^&*)");
            return false;
        }
        if (!digitPattern.matcher(password).find()) {
            loginPassword.setError("Include at least one number (0-9)");
            return false;
        }
        return true;
    }

    private void handleLoginError(Exception exception) {
        if (exception instanceof FirebaseAuthInvalidUserException) {
            showToast("User not found. Please sign up.");
        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            showToast("Incorrect password. Try again.");
        } else {
            showToast("Login Failed: " + exception.getMessage());
        }
    }

    private void showToast(String message) {
        if (toast != null) toast.cancel();
        toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.show();
    }

    private void setupGoogleSignIn() {
        GoogleSignInOptions gOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        gClient = GoogleSignIn.getClient(this, gOptions);

        googleBtn.setOnClickListener(view -> {
            Intent signInIntent = gClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });
    }

    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        checkUserFirestore(account.getId(), account.getEmail()); // Check role for Google Sign-In
                    } catch (ApiException e) {
                        showToast("Google Sign-In Failed");
                    }
                }
            }
    );

    private void navigateToMainActivity() {
        Log.d("NavigationDebug", "Redirecting to MainActivity");
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish(); // Close the LoginActivity
    }

    private void navigateToAdminDashboard() {
        Log.d("NavigationDebug", "Redirecting to AdminDashboardActivity");
        Intent intent = new Intent(this, AdminDashboardActivity.class);
        startActivity(intent);
        finish(); // Close the LoginActivity
    }
}