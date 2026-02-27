package com.example.lankasmartmart.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.lankasmartmart.databinding.ItemCartBinding;
import com.example.lankasmartmart.model.CartItem;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<CartItem> cartItems;
    private Runnable onCartUpdated;

    public CartAdapter(List<CartItem> cartItems, Runnable onCartUpdated) {
        this.cartItems = cartItems;
        this.onCartUpdated = onCartUpdated;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCartBinding binding = ItemCartBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new CartViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        holder.bind(cartItems.get(position));
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    class CartViewHolder extends RecyclerView.ViewHolder {
        private ItemCartBinding binding;

        public CartViewHolder(ItemCartBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(CartItem cartItem) {
            binding.tvProductName.setText(cartItem.getProduct().getName());
            binding.tvProductPrice.setText(String.format("Rs. %.2f", cartItem.getProduct().getPrice()));
            binding.tvQuantity.setText("Qty: " + cartItem.getQuantity());
            binding.imgProduct.setImageResource(cartItem.getProduct().getImageResourceId());
            binding.tvTotalItemPrice.setText(String.format("Rs. %.2f", cartItem.getTotalPrice()));
        }
    }
}
