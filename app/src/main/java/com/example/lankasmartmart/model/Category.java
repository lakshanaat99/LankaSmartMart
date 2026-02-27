package com.example.lankasmartmart.model;

import androidx.annotation.DrawableRes;

public class Category {
    private int id;
    private String name;
    @DrawableRes
    private int iconResourceId;

    public Category(int id, String name, int iconResourceId) {
        this.id = id;
        this.name = name;
        this.iconResourceId = iconResourceId;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getIconResourceId() {
        return iconResourceId;
    }
}
