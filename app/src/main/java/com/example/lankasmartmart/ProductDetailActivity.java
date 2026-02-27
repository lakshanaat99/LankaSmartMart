package com.example.lankasmartmart;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.lankasmartmart.data.DataRepository;
import com.example.lankasmartmart.databinding.ActivityProductDetailBinding;
import com.example.lankasmartmart.model.Product;

public class ProductDetailActivity extends AppCompatActivity {

    private ActivityProductDetailBinding binding;
    private int quantity = 1;
    private Product product;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        int productId = getIntent().getIntExtra("PRODUCT_ID", -1);
        DataRepository repository = DataRepository.getInstance(this);

        if (productId != -1) {
            repository.getProductById(productId).observe(this, product -> {
                if (product != null) {
                    this.product = product;
                    bindProductData();
                } else {
                    Toast.makeText(this, "Product not found", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        } else {
            Toast.makeText(this, "Product ID not provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnIncrease.setOnClickListener(v -> {
            quantity++;
            updateQuantity();
        });

        binding.btnDecrease.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                updateQuantity();
            }
        });

        binding.btnAddToCart.setOnClickListener(v -> {
            if (product != null) {
                repository.addToCart(product, quantity);
                Toast.makeText(this, "Added to Cart!", Toast.LENGTH_SHORT).show();
            }
            finish();
        });
    }

    private void bindProductData() {
        binding.tvProductName.setText(product.getName());
        binding.tvProductPrice.setText(String.format("Rs. %.2f", product.getPrice()));
        binding.tvDescription.setText(product.getDescription());
        binding.imgProduct.setImageResource(product.getImageResourceId());
        updateQuantity();
    }

    private void updateQuantity() {
        binding.tvQuantity.setText(String.valueOf(quantity));
    }
}
