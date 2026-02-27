package com.example.lankasmartmart.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.lankasmartmart.databinding.ItemCartBinding;
import com.example.lankasmartmart.model.CartItem;

import java.util.ArrayList;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<CartItem> cartItems;
    private Runnable updateTotalCallback;

    public CartAdapter(List<CartItem> cartItems, Runnable updateTotalCallback) {
        this.cartItems = cartItems != null ? cartItems : new ArrayList<>();
        this.updateTotalCallback = updateTotalCallback;
    }

    public void updateItems(List<CartItem> newItems) {
        this.cartItems = newItems != null ? newItems : new ArrayList<>();
        notifyDataSetChanged();
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

        public void bind(CartItem item) {
            binding.tvProductName.setText(item.getProduct().getName());
            binding.tvProductPrice.setText(String.format("Rs. %.2f", item.getProduct().getPrice()));
            binding.tvQuantity.setText(String.valueOf(item.getQuantity()));
            binding.imgProduct.setImageResource(item.getProduct().getImageResourceId());

            // Disable buttons for now since Room handles updates via DataRepository
            // separately
            // To be fully reactive, these plus/minus buttons should call
            // DataRepository.updateCart()
        }
    }
}
