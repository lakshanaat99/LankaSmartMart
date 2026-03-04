package com.example.lankasmartmart;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.lankasmartmart.data.DataRepository;
import com.example.lankasmartmart.databinding.ActivityProductDetailBinding;
import com.example.lankasmartmart.model.Product;

public class ProductDetailActivity extends AppCompatActivity {

    // These variables link the code to the screen layout
    private ActivityProductDetailBinding binding;
    private int quantity = 1; // Start with 1 item by default
    private Product product;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Figure out which product the user actually clicked on
        int productId = getIntent().getIntExtra("PRODUCT_ID", -1);
        DataRepository repository = DataRepository.getInstance(this);

        if (productId != -1) {
            // Ask our database to find this specific product
            repository.getProductById(productId).observe(this, product -> {
                if (product != null) {
                    this.product = product;
                    bindProductData(); // Show the details on screen
                    repository.addToRecentlyViewed(product); // Remember that they looked at this
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

        // When the back button is clicked, just close this screen
        binding.btnBack.setOnClickListener(v -> finish());

        // When they want to buy more than one
        binding.btnIncrease.setOnClickListener(v -> {
            quantity++;
            updateQuantity();
        });

        // When they want to buy less
        binding.btnDecrease.setOnClickListener(v -> {
            if (quantity > 1) { // We can't buy zero items!
                quantity--;
                updateQuantity();
            }
        });

        // What happens when they click "Add to Cart"
        binding.btnAddToCart.setOnClickListener(v -> {
            if (product != null) {
                repository.addToCart(product, quantity); // Save it to the cart
                Toast.makeText(this, "Added to Cart!", Toast.LENGTH_SHORT).show();
            }
            finish(); // Close the screen and go back
        });
    }

    // A helper method that grabs the text and images and puts them on the screen
    private void bindProductData() {
        binding.tvProductName.setText(product.getName());
        binding.tvProductPrice.setText(String.format("Rs. %.2f", product.getPrice()));
        binding.tvDescription.setText(product.getDescription());

        // Check if we actually have any left to sell
        if (product.isAvailable()) {
            binding.tvAvailability.setText("In Stock");
            binding.tvAvailability.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            binding.btnAddToCart.setEnabled(true);
            binding.btnAddToCart.setAlpha(1.0f); // Make the button solid
        } else {
            binding.tvAvailability.setText("Out of Stock");
            binding.tvAvailability.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            binding.btnAddToCart.setEnabled(false);
            binding.btnAddToCart.setAlpha(0.5f); // Make the button faded out
        }

        String imageUrl = product.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            // Ask Glide (an image loader tool) to grab the picture from the internet
            Glide.with(this)
                    .load(imageUrl)
                    .into(binding.imgProduct);
        } else {
            // If there's no picture, show a default icon
            binding.imgProduct.setImageResource(android.R.drawable.ic_menu_report_image);
        }

        updateQuantity();
    }

    private void updateQuantity() {
        binding.tvQuantity.setText(String.valueOf(quantity));
    }
}
