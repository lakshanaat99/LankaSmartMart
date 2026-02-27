package com.example.lankasmartmart.model;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.DrawableRes;

public class Product implements Parcelable {
    private int id;
    private String name;
    private String description;
    private double price;
    @DrawableRes
    private int imageResourceId;
    private int categoryId;

    public Product(int id, String name, String description, double price, int imageResourceId, int categoryId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageResourceId = imageResourceId;
        this.categoryId = categoryId;
    }

    protected Product(Parcel in) {
        id = in.readInt();
        name = in.readString();
        description = in.readString();
        price = in.readDouble();
        imageResourceId = in.readInt();
        categoryId = in.readInt();
    }

    public static final Creator<Product> CREATOR = new Creator<Product>() {
        @Override
        public Product createFromParcel(Parcel in) {
            return new Product(in);
        }

        @Override
        public Product[] newArray(int size) {
            return new Product[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(description);
        dest.writeDouble(price);
        dest.writeInt(imageResourceId);
        dest.writeInt(categoryId);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public double getPrice() {
        return price;
    }

    public int getImageResourceId() {
        return imageResourceId;
    }

    public int getCategoryId() {
        return categoryId;
    }
}
