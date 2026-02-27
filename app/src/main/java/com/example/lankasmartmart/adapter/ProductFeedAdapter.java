package com.example.lankasmartmart.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.lankasmartmart.databinding.ItemProductCardBinding;
import com.example.lankasmartmart.model.Product;

import java.util.List;

public class ProductFeedAdapter extends RecyclerView.Adapter<ProductFeedAdapter.ProductViewHolder> {

    private List<Product> products;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Product product);
    }

    public ProductFeedAdapter(List<Product> products, OnItemClickListener listener) {
        this.products = products;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemProductCardBinding binding = ItemProductCardBinding.inflate(LayoutInflater.from(parent.getContext()),
                parent, false);
        return new ProductViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        holder.bind(products.get(position));
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    class ProductViewHolder extends RecyclerView.ViewHolder {
        private ItemProductCardBinding binding;

        public ProductViewHolder(ItemProductCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Product product) {
            binding.tvProductName.setText(product.getName());
            binding.tvProductPrice.setText(String.format("Rs. %.2f", product.getPrice()));
            binding.imgProduct.setImageResource(product.getImageResourceId());

            // Click listener for the entire card
            binding.getRoot().setOnClickListener(v -> listener.onItemClick(product));
        }
    }
}
