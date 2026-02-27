package com.example.lankasmartmart.data.sync;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.lankasmartmart.data.local.AppDatabase;
import com.example.lankasmartmart.data.local.LocalCartEntity;
import com.example.lankasmartmart.data.local.LocalDatabaseDao;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SyncWorker extends Worker {

    private static final String TAG = "SyncWorker";

    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "SyncWorker started");

        LocalDatabaseDao dao = AppDatabase.getDatabase(getApplicationContext()).localDatabaseDao();
        List<LocalCartEntity> unsyncedItems = dao.getUnsyncedCartItems();

        if (unsyncedItems.isEmpty()) {
            Log.d(TAG, "No unsynced items found.");
            return Result.success();
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // In a real app, you'd get the actual logged-in user ID
        String userId = "test_user_123";

        // Group items into an order (simplified for demonstration)
        List<Map<String, Object>> itemsList = new ArrayList<>();
        List<Integer> idsToMarkSynced = new ArrayList<>();

        for (LocalCartEntity item : unsyncedItems) {
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("productId", item.getProductId());
            itemMap.put("quantity", item.getQuantity());
            itemsList.add(itemMap);
            idsToMarkSynced.add(item.getId());
        }

        Map<String, Object> orderData = new HashMap<>();
        orderData.put("userId", userId);
        orderData.put("items", itemsList);
        orderData.put("status", "Pending");
        orderData.put("orderDate", System.currentTimeMillis());

        try {
            // Push to Firestore (Blocking call in a background worker)
            db.collection("Orders").add(orderData);

            // Mark items as synced locally
            dao.markCartItemsAsSynced(idsToMarkSynced);
            Log.d(TAG, "Successfully synced " + unsyncedItems.size() + " items to Firestore.");

            return Result.success();
        } catch (Exception e) {
            Log.e(TAG, "Error syncing to Firestore", e);
            return Result.retry();
        }
    }
}
