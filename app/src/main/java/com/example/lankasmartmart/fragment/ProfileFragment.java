package com.example.lankasmartmart.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.lankasmartmart.LoginActivity;
import com.example.lankasmartmart.R;
import com.example.lankasmartmart.adapter.OrderAdapter;
import com.example.lankasmartmart.auth.AuthManager;
import com.example.lankasmartmart.data.DataRepository;
import com.example.lankasmartmart.databinding.FragmentProfileBinding;
import com.example.lankasmartmart.model.User;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import android.net.Uri;
import android.provider.MediaStore;
import android.app.Activity;
import java.util.UUID;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private DataRepository repository;
    private AuthManager authManager;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private Uri cameraImageUri;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        repository = DataRepository.getInstance(requireContext());
        authManager = AuthManager.getInstance(requireContext());

        // Check if user is logged in
        if (!authManager.isUserLoggedIn()) {
            // Redirect to login if not authenticated
            Intent intent = new Intent(getContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            if (getActivity() != null) {
                getActivity().finish();
            }
            return;
        }

        // Load user data
        loadUserData();

        binding.recyclerViewOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        OrderAdapter adapter = new OrderAdapter(new java.util.ArrayList<>());
        binding.recyclerViewOrders.setAdapter(adapter);

        FirebaseUser firebaseUser = authManager.getCurrentUser();
        if (firebaseUser != null) {
            repository.getOrders(firebaseUser.getUid()).observe(getViewLifecycleOwner(), orders -> {
                if (orders != null) {
                    adapter.updateOrders(orders);
                }
            });
        }

        binding.btnLogout.setOnClickListener(v -> {
            authManager.signOut(requireContext(), new AuthManager.OnSignOutListener() {
                @Override
                public void onSignOutComplete() {
                    Toast.makeText(getContext(), "Logged out!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getContext(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    if (getActivity() != null) {
                        getActivity().finish();
                    }
                }
            });
        });

        setupImageLaunchers();
        binding.btnEditProfileImage.setOnClickListener(v -> showImageSourceOptions());
    }

    private void setupImageLaunchers() {
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            uploadImageToFirebase(imageUri);
                        }
                    }
                });

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        if (cameraImageUri != null) {
                            uploadImageToFirebase(cameraImageUri);
                        }
                    }
                });
    }

    private void showImageSourceOptions() {
        String[] options = { "Gallery", "Camera" };
        new android.app.AlertDialog.Builder(requireContext())
                .setTitle("Select Image Source")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        galleryLauncher.launch(intent);
                    } else {
                        openCamera();
                    }
                })
                .show();
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Create a temporary file for the camera capture
        android.content.ContentValues values = new android.content.ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Profile Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera");
        cameraImageUri = requireContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                values);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
        cameraLauncher.launch(intent);
    }

    private void uploadImageToFirebase(Uri imageUri) {
        FirebaseUser user = authManager.getCurrentUser();
        if (user == null)
            return;

        Toast.makeText(getContext(), "Uploading...", Toast.LENGTH_SHORT).show();

        StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                .child("profile_pictures/" + user.getUid() + ".jpg");

        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String downloadUrl = uri.toString();
                    updateProfilePhotoUrl(downloadUrl);
                }))
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateProfilePhotoUrl(String photoUrl) {
        FirebaseUser user = authManager.getCurrentUser();
        if (user == null)
            return;

        authManager.updateUserProfile(null, user.getDisplayName(), photoUrl, new AuthManager.OnUpdateListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(getContext(), "Profile updated!", Toast.LENGTH_SHORT).show();
                Glide.with(ProfileFragment.this)
                        .load(photoUrl)
                        .placeholder(R.drawable.user)
                        .circleCrop()
                        .into(binding.ivProfileImage);
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(getContext(), "Failed to update profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserData() {
        FirebaseUser firebaseUser = authManager.getCurrentUser();
        if (firebaseUser != null) {
            // Display basic info from Firebase Auth
            String displayName = firebaseUser.getDisplayName();
            String email = firebaseUser.getEmail();
            String photoUrl = firebaseUser.getPhotoUrl() != null ? firebaseUser.getPhotoUrl().toString() : null;

            binding.tvProfileName.setText(displayName != null ? displayName : "User");
            binding.tvProfileEmail.setText(email != null ? email : "");

            // Load profile image
            if (photoUrl != null && !photoUrl.isEmpty()) {
                Glide.with(this)
                        .load(photoUrl)
                        .placeholder(R.drawable.user)
                        .circleCrop()
                        .into(binding.ivProfileImage);
            } else {
                binding.ivProfileImage.setImageResource(R.drawable.user);
            }

            // Try to get additional user data from Firestore
            authManager.getUserFromFirestore(firebaseUser.getUid(), new AuthManager.OnUserFetchListener() {
                @Override
                public void onUserFetched(User user) {
                    if (user.getName() != null && !user.getName().isEmpty()) {
                        binding.tvProfileName.setText(user.getName());
                    }
                    if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                        binding.tvProfileEmail.setText(user.getEmail());
                    }

                    // Display username if available
                    if (user.getUsername() != null && !user.getUsername().isEmpty()) {
                        String handle = "@" + user.getUsername();
                        binding.tvProfileEmail.setText(user.getEmail() + " • " + handle);
                    }

                    if (user.getPhotoUrl() != null && !user.getPhotoUrl().isEmpty()) {
                        Glide.with(ProfileFragment.this)
                                .load(user.getPhotoUrl())
                                .placeholder(R.drawable.user)
                                .circleCrop()
                                .into(binding.ivProfileImage);
                    }
                }

                @Override
                public void onError(Exception e) {
                    // Use Firebase Auth data as fallback (already set above)
                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
