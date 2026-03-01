package com.example.lankasmartmart.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.lankasmartmart.databinding.ItemProductCardSmallBinding;
import com.example.lankasmartmart.model.Product;

import java.util.Collections;
import java.util.List;

public class RecentProductAdapter extends RecyclerView.Adapter<RecentProductAdapter.RecentViewHolder> {

    private List<Product> products;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Product product);
    }

    public RecentProductAdapter(List<Product> products, OnItemClickListener listener) {
        this.products = products != null ? products : Collections.emptyList();
        this.listener = listener;
    }

    public void updateItems(List<Product> newProducts) {
        this.products = newProducts != null ? newProducts : Collections.emptyList();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemProductCardSmallBinding binding = ItemProductCardSmallBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new RecentViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RecentViewHolder holder, int position) {
        holder.bind(products.get(position));
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    class RecentViewHolder extends RecyclerView.ViewHolder {
        private ItemProductCardSmallBinding binding;

        public RecentViewHolder(ItemProductCardSmallBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Product product) {
            binding.tvProductName.setText(product.getName());
            binding.tvProductPrice.setText(String.format("Rs. %.0f", product.getPrice()));

            String imageUrl = product.getImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(binding.imgProduct.getContext())
                        .load(imageUrl)
                        .into(binding.imgProduct);
            } else {
                binding.imgProduct.setImageResource(android.R.drawable.ic_menu_report_image);
            }

            binding.getRoot().setOnClickListener(v -> listener.onItemClick(product));
        }
    }
}
