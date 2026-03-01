package com.example.lankasmartmart.data.local;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import androidx.lifecycle.LiveData;
import java.util.List;

@Dao
public interface LocalDatabaseDao {

    // --- Local Cart ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCartItem(LocalCartEntity item);

    @Update
    void updateCartItem(LocalCartEntity item);

    @Delete
    void deleteCartItem(LocalCartEntity item);

    @Query("SELECT * FROM local_cart")
    List<LocalCartEntity> getAllCartItems();

    @Query("SELECT * FROM local_cart")
    LiveData<List<LocalCartEntity>> getAllCartItemsLiveData();

    @Query("SELECT * FROM local_cart WHERE isSynced = 0")
    List<LocalCartEntity> getUnsyncedCartItems();

    @Query("UPDATE local_cart SET isSynced = 1 WHERE id IN (:ids)")
    void markCartItemsAsSynced(List<Integer> ids);

    @Query("DELETE FROM local_cart")
    void deleteAllCartItems();

    // --- Recent Products ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertRecentProduct(RecentProductEntity product);

    @Query("SELECT * FROM recent_products ORDER BY viewTimestamp DESC LIMIT 10")
    LiveData<List<RecentProductEntity>> getRecentProductsLiveData();

    @Query("SELECT * FROM recent_products ORDER BY viewTimestamp DESC LIMIT 10")
    List<RecentProductEntity> getRecentProducts();

    // --- Cached Products ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertProducts(List<CachedProductEntity> products);

    @Query("SELECT * FROM cached_products")
    LiveData<List<CachedProductEntity>> getAllProductsLiveData();

    @Query("SELECT * FROM cached_products")
    List<CachedProductEntity> getAllProducts();

    @Query("SELECT * FROM cached_products WHERE categoryId = :catId")
    LiveData<List<CachedProductEntity>> getProductsByCategoryIdLiveData(int catId);

    // --- Cached Categories ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCategories(List<CachedCategoryEntity> categories);

    @Query("SELECT * FROM cached_categories ORDER BY name ASC")
    LiveData<List<CachedCategoryEntity>> getAllCategoriesLiveData();

    @Query("SELECT * FROM cached_categories ORDER BY name ASC")
    List<CachedCategoryEntity> getAllCategories();
}
