package com.example.lankasmartmart.data;

import com.example.lankasmartmart.R;
import com.example.lankasmartmart.model.CartItem;
import com.example.lankasmartmart.model.Category;
import com.example.lankasmartmart.model.Product;
import com.example.lankasmartmart.model.User;

import java.util.ArrayList;
import java.util.List;

public class MockDataProvider {

    public static final List<Category> categories = new ArrayList<>();
    public static final List<Product> products = new ArrayList<>();
    private static final List<CartItem> cart = new ArrayList<>();
    public static final User currentUser = new User("Lakshan", "lakshan@example.com"); // Updated User

    static {
        // Initialize Categories
        categories.add(new Category(1, "Groceries", R.drawable.groceries));
        categories.add(new Category(2, "Household", R.drawable.household));
        categories.add(new Category(3, "Personal Care", R.drawable.personal_care)); // Using existing drawable name,
                                                                                    // ensuring 'personal_care.png' or
                                                                                    // similar exists. Reference said
                                                                                    // 'personal Car.png' (sic) in list,
                                                                                    // need to be careful.
        // The list_dir showed "personal Car.png" and "personal_care.png". The code used
        // R.drawable.personal_care. I should check if personal_care.png exists or if I
        // need to rename.
        // Previous list_dir: "personal_care.png" (7083 bytes). So
        // R.drawable.personal_care is valid.
        categories.add(new Category(4, "Stationery", R.drawable.stationery));

        // Initialize Products

        // 1. Groceries
        products.add(new Product(101, "Fresh Coconut", "Large fresh coconut directly from the farm.", 100.00,
                R.drawable.coconut, 1));
        products.add(new Product(102, "Sliced Bread", "Freshly baked customized sliced bread.", 150.00,
                R.drawable.bread, 1));
        products.add(
                new Product(103, "Olive Oil", "Imported extra virgin olive oil.", 1200.00, R.drawable.oliveoli, 1)); // naming
                                                                                                                     // from
                                                                                                                     // file
                                                                                                                     // list
                                                                                                                     // 'oliveoli.png'
        products.add(new Product(104, "Fresh Milk", "Pasteurized fresh milk.", 300.00, R.drawable.milk, 1));

        // 2. Household
        products.add(new Product(201, "Dog Food", "Premium nutrition for your pet.", 2500.00, R.drawable.dogfood, 2));
        products.add(
                new Product(202, "Water Bottle", "Durable reusable water bottle.", 850.00, R.drawable.waterbottle, 2));
        products.add(new Product(203, "Hammer", "Heavy-duty steel hammer.", 1100.00, R.drawable.hammer, 2));
        products.add(new Product(204, "Iron", "Steam iron for clothes.", 4500.00, R.drawable.iorn, 2)); // naming
                                                                                                        // 'iorn.png'
        products.add(
                new Product(205, "Non-stick Pan", "High quality non-stick frying pan.", 3200.00, R.drawable.pan, 2));

        // 3. Personal Care
        products.add(new Product(301, "Herbal Shampoo", "Organic herbal shampoo for daily use.", 450.00,
                R.drawable.shampoo, 3));
        products.add(new Product(302, "Face Cream", "Moisturizing face cream.", 950.00, R.drawable.cream, 3));
        products.add(
                new Product(303, "Makeup Kit", "Complete mostly essential makeup kit.", 5500.00, R.drawable.makeup, 3));

        // 4. Stationery
        products.add(new Product(401, "Blue Pens (Pack of 5)", "Smooth writing blue ballpoint pens.", 250.00,
                R.drawable.pen, 4)); // 'pen.png'
        products.add(new Product(402, "HB Pencils", "Box of 10 HB pencils.", 120.00, R.drawable.pencil, 4));
        products.add(new Product(403, "Stapler", "Standard office stapler.", 450.00, R.drawable.stapler, 4));
        products.add(new Product(404, "Notebook", "200 page ruled notebook.", 350.00, R.drawable.book, 4));
    }

    public static List<CartItem> getCart() {
        return cart;
    }

    public static List<Product> getProductsByCategory(int categoryId) {
        List<Product> result = new ArrayList<>();
        for (Product p : products) {
            if (p.getCategoryId() == categoryId) {
                result.add(p);
            }
        }
        return result;
    }

    public static Product getProductById(int productId) {
        for (Product p : products) {
            if (p.getId() == productId) {
                return p;
            }
        }
        return null;
    }

    public static void addToCart(Product product, int quantity) {
        CartItem existingItem = null;
        for (CartItem item : cart) {
            if (item.getProduct().getId() == product.getId()) {
                existingItem = item;
                break;
            }
        }

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
        } else {
            cart.add(new CartItem(product, quantity));
        }
    }

    public static double getCartTotal() {
        double total = 0;
        for (CartItem item : cart) {
            total += item.getTotalPrice();
        }
        return total;
    }

    public static void clearCart() {
        cart.clear();
    }
}
