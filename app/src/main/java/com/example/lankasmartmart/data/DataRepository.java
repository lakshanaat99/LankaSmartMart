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
import com.example.lankasmartmart.data.local.CachedCategoryEntity;
import com.example.lankasmartmart.data.local.CachedProductEntity;
import com.example.lankasmartmart.data.local.LocalCartEntity;
import com.example.lankasmartmart.data.local.LocalDatabaseDao;
import com.example.lankasmartmart.data.local.RecentProductEntity;
import com.example.lankasmartmart.data.sync.SyncWorker;
import com.example.lankasmartmart.model.CartItem;
import com.example.lankasmartmart.model.Category;
import com.example.lankasmartmart.model.Order;
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

    private List<Product> cachedProducts = new ArrayList<>();

    // --- Live Cloud Data (Firebase Firestore) ---

    // --- Single Source of Truth (Firestore -> Room -> UI) ---

    public LiveData<List<Category>> getCategories() {
        // 1. Return LiveData from Room immediately
        LiveData<List<CachedCategoryEntity>> roomData = localDao.getAllCategoriesLiveData();

        // 2. Fetch from Firestore in background and update Room
        firestore.collection("Categories").addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e(TAG, "Error fetching categories from Firestore", error);
                return;
            }
            if (value != null) {
                executorService.execute(() -> {
                    List<Category> cloudCategories = value.toObjects(Category.class);
                    List<CachedCategoryEntity> entities = new ArrayList<>();
                    for (Category cat : cloudCategories) {
                        entities.add(new CachedCategoryEntity(String.valueOf(cat.getId()), cat.getName(),
                                cat.getIconUrl(), System.currentTimeMillis()));
                    }
                    localDao.insertCategories(entities);
                });
            }
        });

        // 3. Map Room entities back to domain models
        return Transformations.map(roomData, entities -> {
            List<Category> categories = new ArrayList<>();
            for (CachedCategoryEntity entity : entities) {
                categories.add(new Category(Integer.parseInt(entity.getCategoryId()), entity.getName(),
                        entity.getIconUrl()));
            }
            return categories;
        });
    }

    public LiveData<List<Product>> getProducts() {
        LiveData<List<CachedProductEntity>> roomData = localDao.getAllProductsLiveData();

        firestore.collection("Products").addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e(TAG, "Error fetching products from Firestore", error);
                return;
            }
            if (value != null) {
                executorService.execute(() -> {
                    List<Product> cloudProducts = value.toObjects(Product.class);
                    List<CachedProductEntity> entities = new ArrayList<>();
                    for (Product prod : cloudProducts) {
                        entities.add(new CachedProductEntity(String.valueOf(prod.getId()), prod.getName(),
                                prod.getDescription(), prod.getPrice(), prod.getImageUrl(), prod.getCategoryId(),
                                prod.isAvailable(), System.currentTimeMillis()));
                    }
                    localDao.insertProducts(entities);

                    // Keep memory cache for Cart mapping until completely Room-driven
                    cachedProducts = cloudProducts;
                });
            }
        });

        return Transformations.map(roomData, entities -> {
            List<Product> products = new ArrayList<>();
            for (CachedProductEntity entity : entities) {
                products.add(new Product(Integer.parseInt(entity.getProductId()), entity.getName(),
                        entity.getDescription(), entity.getPrice(), entity.getImageUrl(), entity.getCategoryId(),
                        entity.isAvailable()));
            }
            return products;
        });
    }

    public LiveData<List<Product>> getProductsByCategory(int categoryId) {
        LiveData<List<CachedProductEntity>> roomData = localDao.getProductsByCategoryIdLiveData(categoryId);

        // Fetch updates from Cloud
        firestore.collection("Products").whereEqualTo("categoryId", categoryId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error fetching filtered products", error);
                        return;
                    }
                    if (value != null) {
                        executorService.execute(() -> {
                            List<Product> cloudProducts = value.toObjects(Product.class);
                            List<CachedProductEntity> entities = new ArrayList<>();
                            for (Product prod : cloudProducts) {
                                entities.add(new CachedProductEntity(String.valueOf(prod.getId()), prod.getName(),
                                        prod.getDescription(), prod.getPrice(), prod.getImageUrl(),
                                        prod.getCategoryId(), prod.isAvailable(), System.currentTimeMillis()));
                            }
                            localDao.insertProducts(entities);
                        });
                    }
                });

        return Transformations.map(roomData, entities -> {
            List<Product> products = new ArrayList<>();
            for (CachedProductEntity entity : entities) {
                products.add(new Product(Integer.parseInt(entity.getProductId()), entity.getName(),
                        entity.getDescription(), entity.getPrice(), entity.getImageUrl(), entity.getCategoryId(),
                        entity.isAvailable()));
            }
            return products;
        });
    }

    public void addToRecentlyViewed(Product product) {
        executorService.execute(() -> {
            localDao.insertRecentProduct(
                    new RecentProductEntity(String.valueOf(product.getId()), System.currentTimeMillis()));
        });
    }

    public LiveData<List<Product>> getRecentlyViewed() {
        return Transformations.switchMap(localDao.getRecentProductsLiveData(), recentEntities -> {
            MutableLiveData<List<Product>> productsLiveData = new MutableLiveData<>();
            executorService.execute(() -> {
                List<Product> products = new ArrayList<>();
                List<CachedProductEntity> allCached = localDao.getAllProducts();

                for (RecentProductEntity recent : recentEntities) {
                    for (CachedProductEntity cached : allCached) {
                        if (cached.getProductId().equals(recent.getProductId())) {
                            products.add(new Product(Integer.parseInt(cached.getProductId()), cached.getName(),
                                    cached.getDescription(), cached.getPrice(), cached.getImageUrl(),
                                    cached.getCategoryId(), cached.isAvailable()));
                            break;
                        }
                    }
                }
                productsLiveData.postValue(products);
            });
            return productsLiveData;
        });
    }

    public LiveData<Product> getProductById(int productId) {
        MutableLiveData<Product> liveData = new MutableLiveData<>();
        firestore.collection("Products").whereEqualTo("id", productId)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null || value.isEmpty()) {
                        Log.e(TAG, "Error fetching product by ID", error);
                        return;
                    }
                    liveData.setValue(value.getDocuments().get(0).toObject(Product.class));
                });
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

    public void removeFromCart(Product product) {
        executorService.execute(() -> {
            List<LocalCartEntity> currentItems = localDao.getAllCartItems();
            LocalCartEntity existingItem = null;
            for (LocalCartEntity item : currentItems) {
                if (item.getProductId().equals(String.valueOf(product.getId()))) {
                    existingItem = item;
                    break;
                }
            }

            if (existingItem != null) {
                localDao.deleteCartItem(existingItem);
                Log.d(TAG, "Item removed from local cart.");
                scheduleSync();
            }
        });
    }

    public LiveData<List<CartItem>> getCartItems() {
        return Transformations.map(localDao.getAllCartItemsLiveData(), entities -> {
            List<CartItem> cartItems = new ArrayList<>();
            for (LocalCartEntity entity : entities) {
                // Find product from memory cache
                Product product = null;
                int prodId = Integer.parseInt(entity.getProductId());
                for (Product p : cachedProducts) {
                    if (p.getId() == prodId) {
                        product = p;
                        break;
                    }
                }

                if (product != null) {
                    cartItems.add(new CartItem(product, entity.getQuantity()));
                }
            }
            return cartItems;
        });
    }

    public void placeOrder(List<CartItem> items, double total, String userId, Runnable onComplete) {
        String orderId = firestore.collection("Orders").document().getId();
        Order order = new Order(orderId, userId, items, total, System.currentTimeMillis(), "Pending");

        firestore.collection("Orders").document(orderId).set(order)
                .addOnSuccessListener(aVoid -> {
                    clearCart();
                    onComplete.run();
                })
                .addOnFailureListener(e -> Log.e(TAG, "Order failed", e));
    }

    public LiveData<List<Order>> getOrders(String userId) {
        MutableLiveData<List<Order>> liveData = new MutableLiveData<>();
        firestore.collection("Orders").whereEqualTo("userId", userId)
                .addSnapshotListener((value, error) -> {
                    if (error != null)
                        return;
                    if (value != null) {
                        liveData.setValue(value.toObjects(Order.class));
                    }
                });
        return liveData;
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
