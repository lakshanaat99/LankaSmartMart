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
import com.example.lankasmartmart.data.MockDataProvider;
import com.example.lankasmartmart.databinding.FragmentCartBinding;

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
        adapter = new CartAdapter(MockDataProvider.getCart(), this::updateTotal);
        binding.recyclerViewCart.setAdapter(adapter);

        updateTotal();

        binding.btnCheckout.setOnClickListener(v -> {
            if (MockDataProvider.getCart().isEmpty()) {
                Toast.makeText(getContext(), "Cart is empty!", Toast.LENGTH_SHORT).show();
            } else {
                MockDataProvider.clearCart();
                adapter.notifyDataSetChanged();
                updateTotal();
                Toast.makeText(getContext(), "Order Confirmed!", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateTotal() {
        double total = MockDataProvider.getCartTotal();
        binding.tvTotalAmount.setText(String.format("Rs. %.2f", total));
        binding.tvSubtotalAmount.setText(String.format("Rs. %.2f", total));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
