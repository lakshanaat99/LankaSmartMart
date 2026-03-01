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

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private DataRepository repository;
    private AuthManager authManager;

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
