package com.example.lankasmartmart.data.local;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "local_cart")
public class LocalCartEntity {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @NonNull
    private String productId;

    private int quantity;
    private long dateAdded;
    private boolean isSynced;

    public LocalCartEntity(@NonNull String productId, int quantity, long dateAdded, boolean isSynced) {
        this.productId = productId;
        this.quantity = quantity;
        this.dateAdded = dateAdded;
        this.isSynced = isSynced;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @NonNull
    public String getProductId() {
        return productId;
    }

    public void setProductId(@NonNull String productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public long getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(long dateAdded) {
        this.dateAdded = dateAdded;
    }

    public boolean isSynced() {
        return isSynced;
    }

    public void setSynced(boolean synced) {
        isSynced = synced;
    }
}
