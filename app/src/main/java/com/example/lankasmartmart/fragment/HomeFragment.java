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
import android.widget.Toast;
import com.example.lankasmartmart.adapter.CategoryChipAdapter;
import com.example.lankasmartmart.adapter.ProductFeedAdapter;
import com.example.lankasmartmart.adapter.RecentProductAdapter;
import com.example.lankasmartmart.data.DataRepository;
import com.example.lankasmartmart.databinding.FragmentHomeBinding;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.provider.MediaStore;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;
import java.io.IOException;
import java.util.List;

public class HomeFragment extends Fragment implements SensorEventListener {

    private FragmentHomeBinding binding;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private static final float SHAKE_THRESHOLD = 12.0f;
    private long lastShakeTime;

    private ActivityResultLauncher<Intent> qrScannerLauncher;
    private Uri qrImageUri;

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

        // Setup Sensors
        sensorManager = (SensorManager) requireContext().getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        // Setup QR Scanner
        setupQRScanner();
        binding.imgQR.setOnClickListener(v -> openQRCamera());
    }

    private void setupQRScanner() {
        qrScannerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == android.app.Activity.RESULT_OK && qrImageUri != null) {
                        processQRCode(qrImageUri);
                    }
                });
    }

    private void openQRCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        android.content.ContentValues values = new android.content.ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "QR Scan");
        qrImageUri = requireContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, qrImageUri);
        qrScannerLauncher.launch(intent);
    }

    private void processQRCode(Uri imageUri) {
        try {
            InputImage image = InputImage.fromFilePath(requireContext(), imageUri);
            BarcodeScanner scanner = BarcodeScanning.getClient();

            scanner.process(image)
                    .addOnSuccessListener(barcodes -> {
                        for (Barcode barcode : barcodes) {
                            String rawValue = barcode.getRawValue();
                            if (rawValue != null && !rawValue.isEmpty()) {
                                handleScannedValue(rawValue);
                                return;
                            }
                        }
                        Toast.makeText(getContext(), "No QR/Barcode found", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Scan failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleScannedValue(String value) {
        // Try to find product by scanned ID or barcode string
        // For demonstration, we assume the QR contains the Product ID as an integer
        try {
            int productId = Integer.parseInt(value);
            Intent intent = new Intent(getContext(), ProductDetailActivity.class);
            intent.putExtra("PRODUCT_ID", productId);
            startActivity(intent);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Scanned: " + value, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (sensorManager != null && accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            float acceleration = (float) Math.sqrt(x * x + y * y + z * z) - SensorManager.GRAVITY_EARTH;
            if (acceleration > SHAKE_THRESHOLD) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastShakeTime > 2000) {
                    lastShakeTime = currentTime;
                    onShakeDetected();
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private void onShakeDetected() {
        Toast.makeText(getContext(), "Shake detected! Refreshing offers...", Toast.LENGTH_SHORT).show();
        // Here you could trigger a special offer refresh or scroll to a specific
        // section
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
