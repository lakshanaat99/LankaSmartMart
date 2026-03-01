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
    private String iconUrl;
    private long lastUpdated;

    public CachedCategoryEntity(@NonNull String categoryId, String name, String iconUrl, long lastUpdated) {
        this.categoryId = categoryId;
        this.name = name;
        this.iconUrl = iconUrl;
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

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
