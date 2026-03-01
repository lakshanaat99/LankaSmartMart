package com.example.lankasmartmart.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.lankasmartmart.ProductDetailActivity;
import com.example.lankasmartmart.R;
import com.example.lankasmartmart.R;
import android.widget.Toast;
import com.example.lankasmartmart.adapter.CategoryChipAdapter;
import com.example.lankasmartmart.adapter.ProductFeedAdapter;
import com.example.lankasmartmart.adapter.RecentProductAdapter;
import com.example.lankasmartmart.data.DataRepository;
import com.example.lankasmartmart.databinding.FragmentHomeBinding;
import com.example.lankasmartmart.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup Category Chips (Horizontal)
        binding.recyclerViewCategories
                .setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        DataRepository repository = DataRepository.getInstance(requireContext());

        repository.getCategories().observe(getViewLifecycleOwner(), categories -> {
            binding.recyclerViewCategories.setAdapter(new CategoryChipAdapter(categories, category -> {
                ProductListFragment fragment = new ProductListFragment();
                Bundle args = new Bundle();
                args.putInt("CATEGORY_ID", category.getId());
                args.putString("CATEGORY_NAME", category.getName());
                fragment.setArguments(args);

                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit();
            }));
        });

        // Setup Recently Viewed (Horizontal)
        binding.recyclerViewRecent
                .setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        RecentProductAdapter recentAdapter = new RecentProductAdapter(new java.util.ArrayList<>(), product -> {
            Intent intent = new Intent(getContext(), ProductDetailActivity.class);
            intent.putExtra("PRODUCT_ID", product.getId());
            startActivity(intent);
        });
        binding.recyclerViewRecent.setAdapter(recentAdapter);

        repository.getRecentlyViewed().observe(getViewLifecycleOwner(), recentProducts -> {
            if (recentProducts != null && !recentProducts.isEmpty()) {
                binding.layoutRecentlyViewed.setVisibility(View.VISIBLE);
                recentAdapter.updateItems(recentProducts);
            } else {
                binding.layoutRecentlyViewed.setVisibility(View.GONE);
            }
        });

        // Setup Product Feed (Vertical List for Wide Cards)
        binding.recyclerViewProducts.setLayoutManager(new LinearLayoutManager(getContext()));

        repository.getProducts().observe(getViewLifecycleOwner(), products -> {
            binding.recyclerViewProducts.setAdapter(new ProductFeedAdapter(products, product -> {
                Intent intent = new Intent(getContext(), ProductDetailActivity.class);
                intent.putExtra("PRODUCT_ID", product.getId());
                startActivity(intent);
            }, product -> {
                repository.addToCart(product, 1);
                Toast.makeText(getContext(), product.getName() + " added to Cart", Toast.LENGTH_SHORT).show();
            }));
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
