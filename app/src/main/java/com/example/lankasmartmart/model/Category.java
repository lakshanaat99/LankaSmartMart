package com.example.lankasmartmart.model;

import androidx.annotation.DrawableRes;

public class Category {
    private int id;
    private String name;
    private String iconUrl;

    public Category() {
        // Required empty constructor for Firestore parsing
    }

    public Category(int id, String name, String iconUrl) {
        this.id = id;
        this.name = name;
        this.iconUrl = iconUrl;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getIconUrl() {
        return iconUrl;
    }
}
