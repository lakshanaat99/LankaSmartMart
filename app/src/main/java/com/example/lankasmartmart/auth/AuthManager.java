package com.example.lankasmartmart.auth;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Patterns;

import androidx.annotation.NonNull;

import com.example.lankasmartmart.R;
import com.example.lankasmartmart.model.User;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class AuthManager {
    private static AuthManager instance;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private GoogleSignInClient googleSignInClient;
    private static final int RC_SIGN_IN = 9001;

    // Password validation patterns
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");

    // Username validation pattern (alphanumeric, underscore, 3-20 characters)
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");

    private AuthManager(Context context) {
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Configure Google Sign-In
        String webClientId = context.getString(R.string.default_web_client_id);
        GoogleSignInOptions.Builder gsoBuilder = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail();

        // Only add requestIdToken if web client ID is configured
        if (webClientId != null && !webClientId.equals("YOUR_WEB_CLIENT_ID_HERE") && !webClientId.isEmpty()) {
            gsoBuilder.requestIdToken(webClientId);
        }

        googleSignInClient = GoogleSignIn.getClient(context, gsoBuilder.build());
    }

    public static synchronized AuthManager getInstance(Context context) {
        if (instance == null) {
            instance = new AuthManager(context);
        }
        return instance;
    }

    public FirebaseAuth getFirebaseAuth() {
        return firebaseAuth;
    }

    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }

    public boolean isUserLoggedIn() {
        return getCurrentUser() != null;
    }

    // Validate email format
    public boolean isValidEmail(String email) {
        return email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    // Validate password strength
    public PasswordValidationResult validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            return new PasswordValidationResult(false, "Password cannot be empty");
        }

        if (password.length() < MIN_PASSWORD_LENGTH) {
            return new PasswordValidationResult(false,
                    "Password must be at least " + MIN_PASSWORD_LENGTH + " characters long");
        }

        if (!password.matches(".*[a-z].*")) {
            return new PasswordValidationResult(false, "Password must contain at least one lowercase letter");
        }

        if (!password.matches(".*[A-Z].*")) {
            return new PasswordValidationResult(false, "Password must contain at least one uppercase letter");
        }

        if (!password.matches(".*\\d.*")) {
            return new PasswordValidationResult(false, "Password must contain at least one number");
        }

        if (!password.matches(".*[@$!%*?&].*")) {
            return new PasswordValidationResult(false,
                    "Password must contain at least one special character (@$!%*?&)");
        }

        return new PasswordValidationResult(true, "Password is valid");
    }

    // Validate username
    public UsernameValidationResult validateUsername(String username) {
        if (username == null || username.isEmpty()) {
            return new UsernameValidationResult(false, "Username cannot be empty");
        }

        if (username.length() < 3) {
            return new UsernameValidationResult(false, "Username must be at least 3 characters long");
        }

        if (username.length() > 20) {
            return new UsernameValidationResult(false, "Username must be at most 20 characters long");
        }

        if (!USERNAME_PATTERN.matcher(username).matches()) {
            return new UsernameValidationResult(false,
                    "Username can only contain letters, numbers, and underscores");
        }

        return new UsernameValidationResult(true, "Username is valid");
    }

    // Check if username is available
    public void checkUsernameAvailability(String username, OnUsernameCheckListener listener) {
        firestore.collection("users")
                .whereEqualTo("username", username.toLowerCase())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            listener.onUsernameCheck(false, "Username is already taken");
                        } else {
                            listener.onUsernameCheck(true, "Username is available");
                        }
                    } else {
                        listener.onUsernameCheck(false, "Error checking username availability");
                    }
                });
    }

    // Email/Password Sign Up with username
    public void signUpWithEmailPassword(String username, String name, String email, String password,
            OnAuthCompleteListener listener) {
        // Validate inputs
        UsernameValidationResult usernameValidation = validateUsername(username);
        if (!usernameValidation.isValid()) {
            listener.onError(new Exception(usernameValidation.getMessage()));
            return;
        }

        if (!isValidEmail(email)) {
            listener.onError(new Exception("Invalid email address"));
            return;
        }

        PasswordValidationResult passwordValidation = validatePassword(password);
        if (!passwordValidation.isValid()) {
            listener.onError(new Exception(passwordValidation.getMessage()));
            return;
        }

        // Check username availability
        checkUsernameAvailability(username, new OnUsernameCheckListener() {
            @Override
            public void onUsernameCheck(boolean available, String message) {
                if (!available) {
                    listener.onError(new Exception(message));
                    return;
                }

                // Create user with email and password
                firebaseAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                                if (firebaseUser != null) {
                                    // Update user profile in Firebase Auth
                                    com.google.firebase.auth.UserProfileChangeRequest profileUpdates = new com.google.firebase.auth.UserProfileChangeRequest.Builder()
                                            .setDisplayName(name)
                                            .build();
                                    firebaseUser.updateProfile(profileUpdates);

                                    // Save user to Firestore with username
                                    saveUserToFirestore(firebaseUser, username, name, null);
                                    listener.onSuccess(firebaseUser);
                                }
                            } else {
                                listener.onError(task.getException());
                            }
                        });
            }
        });
    }

    // Email/Password Sign In
    public void signInWithEmailPassword(String email, String password,
            OnAuthCompleteListener listener) {
        if (!isValidEmail(email)) {
            listener.onError(new Exception("Invalid email address"));
            return;
        }

        if (password == null || password.isEmpty()) {
            listener.onError(new Exception("Password cannot be empty"));
            return;
        }

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            // Update last login time
                            updateLastLogin(firebaseUser.getUid());
                            listener.onSuccess(firebaseUser);
                        }
                    } else {
                        listener.onError(task.getException());
                    }
                });
    }

    // Sign Out
    public void signOut(Context context, OnSignOutListener listener) {
        firebaseAuth.signOut();
        googleSignInClient.signOut().addOnCompleteListener(task -> {
            listener.onSignOutComplete();
        });
    }

    // Google Sign-In
    public Intent getGoogleSignInIntent() {
        return googleSignInClient.getSignInIntent();
    }

    public void handleGoogleSignInResult(Intent data, Activity activity,
            OnAuthCompleteListener listener) {
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            if (account != null) {
                firebaseAuthWithGoogle(account.getIdToken(), account, listener);
            }
        } catch (ApiException e) {
            listener.onError(e);
        }
    }

    private void firebaseAuthWithGoogle(String idToken, GoogleSignInAccount account,
            OnAuthCompleteListener listener) {
        if (idToken == null) {
            listener.onError(
                    new Exception("Google Sign-In failed. Please configure Web Client ID in Firebase Console."));
            return;
        }

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            // Check if user exists in Firestore, if not create
                            checkAndSaveGoogleUser(firebaseUser, account.getDisplayName(),
                                    account.getPhotoUrl() != null ? account.getPhotoUrl().toString() : null);
                            // Update last login time
                            updateLastLogin(firebaseUser.getUid());
                            listener.onSuccess(firebaseUser);
                        }
                    } else {
                        listener.onError(task.getException());
                    }
                });
    }

    // Check if Google user exists in Firestore, if not create
    private void checkAndSaveGoogleUser(FirebaseUser firebaseUser, String name, String photoUrl) {
        firestore.collection("users")
                .document(firebaseUser.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document == null || !document.exists()) {
                            // User doesn't exist, create new
                            // Generate username from email or name
                            String username = generateUsernameFromEmail(firebaseUser.getEmail());
                            saveUserToFirestore(firebaseUser, username, name, photoUrl);
                        } else {
                            // User exists, update last login and photo if changed
                            Map<String, Object> updates = new HashMap<>();
                            updates.put("updatedAt", com.google.firebase.Timestamp.now());
                            updates.put("lastLogin", com.google.firebase.Timestamp.now());
                            if (photoUrl != null && !photoUrl.isEmpty()) {
                                updates.put("photoUrl", photoUrl);
                            }
                            firestore.collection("users")
                                    .document(firebaseUser.getUid())
                                    .update(updates);
                        }
                    }
                });
    }

    // Generate username from email for Google Sign-In users
    private String generateUsernameFromEmail(String email) {
        if (email == null || email.isEmpty()) {
            return "user_" + System.currentTimeMillis();
        }
        String username = email.split("@")[0].toLowerCase();
        // Remove invalid characters
        username = username.replaceAll("[^a-z0-9_]", "");
        // Ensure it meets requirements
        if (username.length() < 3) {
            username = username + "_" + System.currentTimeMillis();
        }
        if (username.length() > 20) {
            username = username.substring(0, 20);
        }
        return username;
    }

    // Save user to Firestore
    private void saveUserToFirestore(FirebaseUser firebaseUser, String username, String name, String photoUrl) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("uid", firebaseUser.getUid());
        userData.put("username", username != null ? username.toLowerCase() : "");
        userData.put("name", name != null ? name : firebaseUser.getDisplayName());
        userData.put("email", firebaseUser.getEmail());
        userData.put("photoUrl", photoUrl != null ? photoUrl : "");
        userData.put("createdAt", com.google.firebase.Timestamp.now());
        userData.put("updatedAt", com.google.firebase.Timestamp.now());
        userData.put("lastLogin", com.google.firebase.Timestamp.now());
        userData.put("isActive", true);
        userData.put("role", "customer"); // Add default role

        firestore.collection("users")
                .document(firebaseUser.getUid())
                .set(userData)
                .addOnFailureListener(e -> {
                    // Log error but don't block authentication
                });
    }

    // Update last login time
    private void updateLastLogin(String uid) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("lastLogin", com.google.firebase.Timestamp.now());
        updates.put("updatedAt", com.google.firebase.Timestamp.now());

        firestore.collection("users")
                .document(uid)
                .update(updates)
                .addOnFailureListener(e -> {
                    // Log error silently
                });
    }

    // Get user from Firestore
    public void getUserFromFirestore(String uid, OnUserFetchListener listener) {
        firestore.collection("users")
                .document(uid)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            String username = document.getString("username");
                            String name = document.getString("name");
                            String email = document.getString("email");
                            String photoUrl = document.getString("photoUrl");
                            User user = new User(uid, username, name, email, photoUrl);
                            listener.onUserFetched(user);
                        } else {
                            // Fallback to Firebase Auth user
                            FirebaseUser firebaseUser = getCurrentUser();
                            if (firebaseUser != null) {
                                User user = new User(firebaseUser.getUid(),
                                        null,
                                        firebaseUser.getDisplayName(),
                                        firebaseUser.getEmail(),
                                        null);
                                listener.onUserFetched(user);
                            } else {
                                listener.onError(new Exception("User not found"));
                            }
                        }
                    } else {
                        listener.onError(task.getException());
                    }
                });
    }

    // Update user profile
    public void updateUserProfile(String username, String name, String photoUrl, OnUpdateListener listener) {
        FirebaseUser firebaseUser = getCurrentUser();
        if (firebaseUser == null) {
            listener.onError(new Exception("User not logged in"));
            return;
        }

        // Validate username if provided
        if (username != null && !username.isEmpty()) {
            UsernameValidationResult usernameValidation = validateUsername(username);
            if (!usernameValidation.isValid()) {
                listener.onError(new Exception(usernameValidation.getMessage()));
                return;
            }

            // Check if username is available (if different from current)
            checkUsernameAvailability(username, new OnUsernameCheckListener() {
                @Override
                public void onUsernameCheck(boolean available, String message) {
                    if (!available) {
                        listener.onError(new Exception(message));
                        return;
                    }
                    performProfileUpdate(firebaseUser, username, name, photoUrl, listener);
                }
            });
        } else {
            performProfileUpdate(firebaseUser, null, name, photoUrl, listener);
        }
    }

    private void performProfileUpdate(FirebaseUser firebaseUser, String username, String name,
            String photoUrl, OnUpdateListener listener) {
        // Update Firebase Auth profile
        com.google.firebase.auth.UserProfileChangeRequest profileUpdates = new com.google.firebase.auth.UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .setPhotoUri(photoUrl != null ? Uri.parse(photoUrl) : null)
                .build();

        firebaseUser.updateProfile(profileUpdates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Update Firestore
                        Map<String, Object> updates = new HashMap<>();
                        if (username != null && !username.isEmpty()) {
                            updates.put("username", username.toLowerCase());
                        }
                        if (name != null) {
                            updates.put("name", name);
                        }
                        if (photoUrl != null) {
                            updates.put("photoUrl", photoUrl);
                        }
                        updates.put("updatedAt", com.google.firebase.Timestamp.now());

                        firestore.collection("users")
                                .document(firebaseUser.getUid())
                                .update(updates)
                                .addOnCompleteListener(updateTask -> {
                                    if (updateTask.isSuccessful()) {
                                        listener.onSuccess();
                                    } else {
                                        listener.onError(updateTask.getException());
                                    }
                                });
                    } else {
                        listener.onError(task.getException());
                    }
                });
    }

    // Change password
    public void changePassword(String currentPassword, String newPassword, OnPasswordChangeListener listener) {
        FirebaseUser firebaseUser = getCurrentUser();
        if (firebaseUser == null) {
            listener.onError(new Exception("User not logged in"));
            return;
        }

        // Validate new password
        PasswordValidationResult passwordValidation = validatePassword(newPassword);
        if (!passwordValidation.isValid()) {
            listener.onError(new Exception(passwordValidation.getMessage()));
            return;
        }

        // Re-authenticate user before changing password
        AuthCredential credential = EmailAuthProvider.getCredential(firebaseUser.getEmail(), currentPassword);

        firebaseUser.reauthenticate(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Update password
                        firebaseUser.updatePassword(newPassword)
                                .addOnCompleteListener(updateTask -> {
                                    if (updateTask.isSuccessful()) {
                                        listener.onSuccess();
                                    } else {
                                        listener.onError(updateTask.getException());
                                    }
                                });
                    } else {
                        listener.onError(new Exception("Current password is incorrect"));
                    }
                });
    }

    // Password reset
    public void sendPasswordResetEmail(String email, OnPasswordResetListener listener) {
        if (!isValidEmail(email)) {
            listener.onError(new Exception("Invalid email address"));
            return;
        }

        firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        listener.onSuccess();
                    } else {
                        listener.onError(task.getException());
                    }
                });
    }

    // Validation result classes
    public static class PasswordValidationResult {
        private boolean valid;
        private String message;

        public PasswordValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }
    }

    public static class UsernameValidationResult {
        private boolean valid;
        private String message;

        public UsernameValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }
    }

    // Interfaces
    public interface OnAuthCompleteListener {
        void onSuccess(FirebaseUser user);

        void onError(Exception e);
    }

    public interface OnSignOutListener {
        void onSignOutComplete();
    }

    public interface OnUserFetchListener {
        void onUserFetched(User user);

        void onError(Exception e);
    }

    public interface OnUpdateListener {
        void onSuccess();

        void onError(Exception e);
    }

    public interface OnUsernameCheckListener {
        void onUsernameCheck(boolean available, String message);
    }

    public interface OnPasswordChangeListener {
        void onSuccess();

        void onError(Exception e);
    }

    public interface OnPasswordResetListener {
        void onSuccess();

        void onError(Exception e);
    }
}
