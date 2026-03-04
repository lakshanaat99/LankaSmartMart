package com.example.lankasmartmart;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.lankasmartmart.auth.AuthManager;
import com.example.lankasmartmart.databinding.ActivitySignupBinding;
import com.google.firebase.auth.FirebaseUser;

public class SignupActivity extends AppCompatActivity {

    // These variables connect our code to the user interface
    private ActivitySignupBinding binding;
    private AuthManager authManager; // Helps us create accounts
    private static final int RC_SIGN_IN = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authManager = AuthManager.getInstance(this);

        // Check if user is already logged in securely
        // We don't want logged-in users to see the signup screen
        if (authManager.isUserLoggedIn()) {
            navigateToMain();
            return;
        }

        // What happens when the user clicks the "Sign Up" button
        binding.btnSignup.setOnClickListener(v -> {
            String username = binding.etUsername.getText().toString().trim();
            String name = binding.etName.getText().toString().trim();
            String email = binding.etEmail.getText().toString().trim();
            String password = binding.etPassword.getText().toString();
            String confirmPassword = binding.etConfirmPassword.getText().toString();

            // Clear out any old error messages from before
            clearErrors();

            // We use this flag to check if everything the user typed is okay
            boolean isValid = true;

            if (TextUtils.isEmpty(username)) {
                binding.etUsername.setError("Username is required");
                binding.etUsername.requestFocus();
                isValid = false;
            } else {
                AuthManager.UsernameValidationResult usernameValidation = authManager.validateUsername(username);
                if (!usernameValidation.isValid()) {
                    binding.etUsername.setError(usernameValidation.getMessage());
                    binding.etUsername.requestFocus();
                    isValid = false;
                }
            }

            if (TextUtils.isEmpty(name)) {
                binding.etName.setError("Full name is required");
                if (isValid) {
                    binding.etName.requestFocus();
                    isValid = false;
                }
            }

            if (TextUtils.isEmpty(email)) {
                binding.etEmail.setError("Email is required");
                if (isValid) {
                    binding.etEmail.requestFocus();
                    isValid = false;
                }
            } else if (!authManager.isValidEmail(email)) {
                binding.etEmail.setError("Please enter a valid email address");
                if (isValid) {
                    binding.etEmail.requestFocus();
                    isValid = false;
                }
            }

            if (TextUtils.isEmpty(password)) {
                binding.etPassword.setError("Password is required");
                if (isValid) {
                    binding.etPassword.requestFocus();
                    isValid = false;
                }
            } else {
                AuthManager.PasswordValidationResult passwordValidation = authManager.validatePassword(password);
                if (!passwordValidation.isValid()) {
                    binding.etPassword.setError(passwordValidation.getMessage());
                    if (isValid) {
                        binding.etPassword.requestFocus();
                        isValid = false;
                    }
                }
            }

            if (TextUtils.isEmpty(confirmPassword)) {
                binding.etConfirmPassword.setError("Please confirm your password");
                if (isValid) {
                    binding.etConfirmPassword.requestFocus();
                    isValid = false;
                }
            } else if (!password.equals(confirmPassword)) {
                binding.etConfirmPassword.setError("Passwords do not match");
                if (isValid) {
                    binding.etConfirmPassword.requestFocus();
                    isValid = false;
                }
            }

            // If any of the checks above failed (like a missing password), stop here!
            if (!isValid) {
                return;
            }

            // Show a little spinning loading icon and freeze the button
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.btnSignup.setEnabled(false);

            // Finally, tell Firebase to create the new account
            authManager.signUpWithEmailPassword(username, name, email, password,
                    new AuthManager.OnAuthCompleteListener() {
                        @Override
                        public void onSuccess(FirebaseUser user) {
                            binding.progressBar.setVisibility(View.GONE);
                            binding.btnSignup.setEnabled(true);
                            Toast.makeText(SignupActivity.this, "Account Created Successfully", Toast.LENGTH_SHORT)
                                    .show();
                            navigateToMain();
                        }

                        @Override
                        public void onError(Exception e) {
                            binding.progressBar.setVisibility(View.GONE);
                            binding.btnSignup.setEnabled(true);
                            String errorMessage = "Sign up failed";
                            if (e.getMessage() != null) {
                                if (e.getMessage().contains("invalid-email")) {
                                    errorMessage = "Invalid email address";
                                    binding.etEmail.setError(errorMessage);
                                    binding.etEmail.requestFocus();
                                } else if (e.getMessage().contains("email-already-in-use")) {
                                    errorMessage = "An account with this email already exists";
                                    binding.etEmail.setError(errorMessage);
                                    binding.etEmail.requestFocus();
                                } else if (e.getMessage().contains("weak-password")) {
                                    errorMessage = "Password is too weak";
                                    binding.etPassword.setError(errorMessage);
                                    binding.etPassword.requestFocus();
                                } else if (e.getMessage().contains("network")) {
                                    errorMessage = "Network error. Please check your connection";
                                } else if (e.getMessage().contains("Username is already taken")) {
                                    binding.etUsername.setError(e.getMessage());
                                    binding.etUsername.requestFocus();
                                    errorMessage = e.getMessage();
                                } else {
                                    errorMessage = e.getMessage();
                                }
                            }
                            Toast.makeText(SignupActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // If they already have an account, clicking Login just closes this screen
        binding.tvLogin.setOnClickListener(v -> {
            finish();
        });

        // This cool feature checks if the username is taken AS they are typing it!
        binding.etUsername.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus && !TextUtils.isEmpty(binding.etUsername.getText().toString())) {
                String username = binding.etUsername.getText().toString().trim();
                AuthManager.UsernameValidationResult validation = authManager.validateUsername(username);
                if (validation.isValid()) {
                    // Check availability
                    authManager.checkUsernameAvailability(username, new AuthManager.OnUsernameCheckListener() {
                        @Override
                        public void onUsernameCheck(boolean available, String message) {
                            if (!available) {
                                binding.etUsername.setError(message);
                            }
                        }
                    });
                }
            }
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
                    Toast.makeText(SignupActivity.this, "Account Created Successfully", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(SignupActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // Wipes all the red error messages on the screen
    private void clearErrors() {
        binding.etUsername.setError(null);
        binding.etName.setError(null);
        binding.etEmail.setError(null);
        binding.etPassword.setError(null);
        binding.etConfirmPassword.setError(null);
    }

    // A helper method to switch to the Main application screen
    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        // We clear the history so they can't press back to go to the Signup screen
        // again
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
