package com.example.lankasmartmart.data.local;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "cached_categories")
public class CachedCategoryEntity {

    @PrimaryKey
    @NonNull
    private String categoryId;

    private String name;
    private String imageUrl;
    private long lastUpdated;

    public CachedCategoryEntity(@NonNull String categoryId, String name, String imageUrl, long lastUpdated) {
        this.categoryId = categoryId;
        this.name = name;
        this.imageUrl = imageUrl;
        this.lastUpdated = lastUpdated;
    }

    @NonNull
    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(@NonNull String categoryId) {
        this.categoryId = categoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
