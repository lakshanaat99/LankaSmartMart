package com.example.lankasmartmart;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.lankasmartmart.auth.AuthManager;
import com.example.lankasmartmart.databinding.ActivityLoginBinding;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;
import android.util.Log;

public class LoginActivity extends AppCompatActivity {

    // View binding holds references to our UI (buttons, text fields)
    private ActivityLoginBinding binding;

    // AuthManager handles talking to Firebase for login/signup
    private AuthManager authManager;
    private static final int RC_SIGN_IN = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Connect this code to the activity_login.xml design file
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authManager = AuthManager.getInstance(this);

        // Fetch and log FCM token just in case user is stuck on Login screen
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.e("FCM_TOKEN", "Fetching FCM registration token failed in LoginActivity",
                                task.getException());
                        return;
                    }
                    String token = task.getResult();
                    Log.e("FCM_TOKEN", "FCM Registration Token: " + token);
                    System.out.println("========== FCM Registration Token: " + token + " ==========");
                });

        // Check if user is already logged in from a previous session
        // If they are, skip login and go straight to the Home screen
        if (authManager.isUserLoggedIn()) {
            navigateToMain();
            return;
        }

        // What happens when the user clicks the "Log In" button
        binding.btnLogin.setOnClickListener(v -> {
            String email = binding.etEmail.getText().toString().trim();
            String password = binding.etPassword.getText().toString();

            // Validate inputs to make sure they didn't leave anything blank
            if (TextUtils.isEmpty(email)) {
                binding.etEmail.setError("Email is required");
                binding.etEmail.requestFocus();
                return;
            }

            if (!authManager.isValidEmail(email)) {
                binding.etEmail.setError("Please enter a valid email address");
                binding.etEmail.requestFocus();
                return;
            }

            if (TextUtils.isEmpty(password)) {
                binding.etPassword.setError("Password is required");
                binding.etPassword.requestFocus();
                return;
            }

            binding.progressBar.setVisibility(View.VISIBLE); // Show loading spinner
            binding.btnLogin.setEnabled(false); // Disable button to prevent double-clicks
            binding.etEmail.setError(null);
            binding.etPassword.setError(null);

            // Ask AuthManager to try logging in with Firebase
            authManager.signInWithEmailPassword(email, password, new AuthManager.OnAuthCompleteListener() {
                @Override
                public void onSuccess(FirebaseUser user) {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnLogin.setEnabled(true);
                    Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                    navigateToMain();
                }

                @Override
                public void onError(Exception e) {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnLogin.setEnabled(true);
                    String errorMessage = "Login failed";
                    if (e.getMessage() != null) {
                        if (e.getMessage().contains("invalid-email")) {
                            errorMessage = "Invalid email address";
                            binding.etEmail.setError(errorMessage);
                            binding.etEmail.requestFocus();
                        } else if (e.getMessage().contains("user-not-found")) {
                            errorMessage = "No account found with this email";
                            binding.etEmail.setError(errorMessage);
                            binding.etEmail.requestFocus();
                        } else if (e.getMessage().contains("wrong-password")) {
                            errorMessage = "Incorrect password";
                            binding.etPassword.setError(errorMessage);
                            binding.etPassword.requestFocus();
                        } else if (e.getMessage().contains("network")) {
                            errorMessage = "Network error. Please check your connection";
                        } else if (e.getMessage().contains("too-many-requests")) {
                            errorMessage = "Too many failed attempts. Please try again later";
                        } else if (e.getMessage().contains("user-disabled")) {
                            errorMessage = "This account has been disabled";
                        } else {
                            errorMessage = e.getMessage();
                        }
                    }
                    Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        });

        // What happens when the user clicks "Sign Up" instead
        binding.tvSignup.setOnClickListener(v -> {
            Intent intent = new Intent(this, SignupActivity.class);
            startActivity(intent);
        });

        binding.tvForgotPassword.setOnClickListener(v -> {
            String email = binding.etEmail.getText().toString().trim();
            if (TextUtils.isEmpty(email)) {
                binding.etEmail.setError("Please enter your email address");
                binding.etEmail.requestFocus();
                return;
            }

            if (!authManager.isValidEmail(email)) {
                binding.etEmail.setError("Please enter a valid email address");
                binding.etEmail.requestFocus();
                return;
            }

            resetPassword(email);
        });

        binding.btnGoogleSignIn.setOnClickListener(v -> {
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.btnGoogleSignIn.setEnabled(false);
            Intent signInIntent = authManager.getGoogleSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            binding.progressBar.setVisibility(View.VISIBLE);
            authManager.handleGoogleSignInResult(data, this, new AuthManager.OnAuthCompleteListener() {
                @Override
                public void onSuccess(FirebaseUser user) {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnGoogleSignIn.setEnabled(true);
                    Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                    navigateToMain();
                }

                @Override
                public void onError(Exception e) {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnGoogleSignIn.setEnabled(true);
                    String errorMessage = "Google Sign-In failed";
                    if (e.getMessage() != null) {
                        if (e.getMessage().contains("12500")) {
                            errorMessage = "Google Sign-In is not configured. Please configure it in Firebase Console.";
                        } else if (e.getMessage().contains("Web Client ID")) {
                            errorMessage = e.getMessage();
                        } else {
                            errorMessage = e.getMessage();
                        }
                    }
                    Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // A helper method to switch to the Main application screen
    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        // Clear history so the user can't press 'back' to return to Login
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void resetPassword(String email) {
        binding.progressBar.setVisibility(View.VISIBLE);
        authManager.sendPasswordResetEmail(email, new AuthManager.OnPasswordResetListener() {
            @Override
            public void onSuccess() {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(LoginActivity.this,
                        "Password reset email sent. Please check your inbox.",
                        Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(Exception e) {
                binding.progressBar.setVisibility(View.GONE);
                String errorMessage = "Failed to send reset email";
                if (e.getMessage() != null) {
                    if (e.getMessage().contains("user-not-found")) {
                        errorMessage = "No account found with this email";
                    } else {
                        errorMessage = e.getMessage();
                    }
                }
                Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
