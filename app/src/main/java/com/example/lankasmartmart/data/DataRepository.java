package com.example.lankasmartmart.data;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.lankasmartmart.data.local.AppDatabase;
import com.example.lankasmartmart.data.local.LocalCartEntity;
import com.example.lankasmartmart.data.local.LocalDatabaseDao;
import com.example.lankasmartmart.data.sync.SyncWorker;
import com.example.lankasmartmart.model.CartItem;
import com.example.lankasmartmart.model.Category;
import com.example.lankasmartmart.model.Product;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DataRepository {

    private static final String TAG = "DataRepository";
    private static DataRepository instance;
    private final LocalDatabaseDao localDao;
    private final FirebaseFirestore firestore;
    private final ExecutorService executorService;
    private final Context context;

    private DataRepository(Context context) {
        this.context = context.getApplicationContext();
        AppDatabase db = AppDatabase.getDatabase(this.context);
        localDao = db.localDatabaseDao();
        firestore = FirebaseFirestore.getInstance();
        executorService = Executors.newFixedThreadPool(4); // For background local DB operations
    }

    public static synchronized DataRepository getInstance(Context context) {
        if (instance == null) {
            instance = new DataRepository(context);
        }
        return instance;
    }

    // --- Mock Data Equivalents (To be replaced with Firestore) ---

    public LiveData<List<Category>> getCategories() {
        MutableLiveData<List<Category>> liveData = new MutableLiveData<>();
        liveData.setValue(MockDataProvider.categories);
        return liveData;
    }

    public LiveData<List<Product>> getProducts() {
        MutableLiveData<List<Product>> liveData = new MutableLiveData<>();
        liveData.setValue(MockDataProvider.products);
        return liveData;
    }

    public LiveData<List<Product>> getProductsByCategory(int categoryId) {
        MutableLiveData<List<Product>> liveData = new MutableLiveData<>();
        liveData.setValue(MockDataProvider.getProductsByCategory(categoryId));
        return liveData;
    }

    public LiveData<Product> getProductById(int productId) {
        MutableLiveData<Product> liveData = new MutableLiveData<>();
        liveData.setValue(MockDataProvider.getProductById(productId));
        return liveData;
    }

    // --- Cart Operations (Room Database) ---

    public void addToCart(Product product, int quantity) {
        executorService.execute(() -> {
            // Check if item already exists in local DB to update quantity
            List<LocalCartEntity> currentItems = localDao.getAllCartItems();
            LocalCartEntity existingItem = null;
            for (LocalCartEntity item : currentItems) {
                if (item.getProductId().equals(String.valueOf(product.getId()))) {
                    existingItem = item;
                    break;
                }
            }

            long currentTime = System.currentTimeMillis();
            if (existingItem != null) {
                existingItem.setQuantity(existingItem.getQuantity() + quantity);
                existingItem.setSynced(false);
                existingItem.setDateAdded(currentTime);
                localDao.updateCartItem(existingItem);
            } else {
                LocalCartEntity cartItem = new LocalCartEntity(String.valueOf(product.getId()), quantity, currentTime,
                        false);
                localDao.insertCartItem(cartItem);
            }

            Log.d(TAG, "Item added/updated in local cart. Scheduling sync.");

            // 2. Schedule WorkManager sync for when network is available
            scheduleSync();
        });
    }

    public LiveData<List<CartItem>> getCartItems() {
        return Transformations.map(localDao.getAllCartItemsLiveData(), entities -> {
            List<CartItem> cartItems = new ArrayList<>();
            for (LocalCartEntity entity : entities) {
                Product product = MockDataProvider.getProductById(Integer.parseInt(entity.getProductId())); // Temporary
                                                                                                            // mapping
                if (product != null) {
                    cartItems.add(new CartItem(product, entity.getQuantity()));
                }
            }
            return cartItems;
        });
    }

    public void clearCart() {
        executorService.execute(() -> {
            localDao.deleteAllCartItems();
            Log.d(TAG, "Local cart cleared");
        });
    }

    private void scheduleSync() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest syncRequest = new OneTimeWorkRequest.Builder(SyncWorker.class)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(context).enqueue(syncRequest);
    }
}
