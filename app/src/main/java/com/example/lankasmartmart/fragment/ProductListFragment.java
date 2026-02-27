package com.example.lankasmartmart.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import com.example.lankasmartmart.ProductDetailActivity;
import com.example.lankasmartmart.R;
import com.example.lankasmartmart.adapter.ProductFeedAdapter;
import com.example.lankasmartmart.data.MockDataProvider;
import com.example.lankasmartmart.databinding.FragmentProductListBinding;
import com.example.lankasmartmart.model.Product;

import java.util.List;

public class ProductListFragment extends Fragment {

    private FragmentProductListBinding binding;
    private int categoryId = -1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentProductListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String categoryName = "Products";
        if (getArguments() != null) {
            categoryId = getArguments().getInt("CATEGORY_ID", -1);
            categoryName = getArguments().getString("CATEGORY_NAME", "Products");
        }

        binding.tvCategoryTitle.setText(categoryName);

        binding.btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        binding.btnCart.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new CartFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // Use Grid Layout for Product List
        binding.recyclerViewProducts.setLayoutManager(new GridLayoutManager(getContext(), 2));
        List<Product> products = (categoryId != -1) ? MockDataProvider.getProductsByCategory(categoryId)
                : MockDataProvider.products;

        binding.recyclerViewProducts.setAdapter(new ProductFeedAdapter(products, product -> {
            Intent intent = new Intent(getContext(), ProductDetailActivity.class);
            intent.putExtra("PRODUCT_ID", product.getId());
            startActivity(intent);
        }));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
