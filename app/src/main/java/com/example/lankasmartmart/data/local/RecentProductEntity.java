package com.example.lankasmartmart.data.local;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "recent_products")
public class RecentProductEntity {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @NonNull
    private String productId;

    private long viewTimestamp;

    public RecentProductEntity(@NonNull String productId, long viewTimestamp) {
        this.productId = productId;
        this.viewTimestamp = viewTimestamp;
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

    public long getViewTimestamp() {
        return viewTimestamp;
    }

    public void setViewTimestamp(long viewTimestamp) {
        this.viewTimestamp = viewTimestamp;
    }
}
