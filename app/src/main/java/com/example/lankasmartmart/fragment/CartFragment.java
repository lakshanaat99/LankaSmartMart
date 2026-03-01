package com.example.lankasmartmart.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.lankasmartmart.adapter.CartAdapter;
import com.example.lankasmartmart.data.DataRepository;
import com.example.lankasmartmart.databinding.FragmentCartBinding;
import com.example.lankasmartmart.model.CartItem;

import java.util.ArrayList;

public class CartFragment extends Fragment {

    private FragmentCartBinding binding;
    private CartAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentCartBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.recyclerViewCart.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CartAdapter(new ArrayList<>(), () -> {
        }, item -> {
            DataRepository.getInstance(requireContext()).removeFromCart(item.getProduct());
            Toast.makeText(getContext(), "Item removed", Toast.LENGTH_SHORT).show();
        });
        binding.recyclerViewCart.setAdapter(adapter);

        DataRepository repository = DataRepository.getInstance(requireContext());

        repository.getCartItems().observe(getViewLifecycleOwner(), cartItems -> {
            adapter.updateItems(cartItems);
            updateTotal(cartItems);

            binding.btnCheckout.setOnClickListener(v -> {
                if (cartItems.isEmpty()) {
                    Toast.makeText(getContext(), "Cart is empty!", Toast.LENGTH_SHORT).show();
                } else {
                    double total = 0;
                    for (CartItem item : cartItems)
                        total += item.getTotalPrice();

                    repository.placeOrder(cartItems, total, "user_123", () -> {
                        Toast.makeText(getContext(), "Order Confirmed!", Toast.LENGTH_LONG).show();
                    });
                }
            });
        });
    }

    private void updateTotal(java.util.List<CartItem> items) {
        double total = 0;
        if (items != null) {
            for (CartItem item : items) {
                total += item.getTotalPrice();
            }
        }
        binding.tvTotalAmount.setText(String.format("Rs. %.2f", total));
        binding.tvSubtotalAmount.setText(String.format("Rs. %.2f", total));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
