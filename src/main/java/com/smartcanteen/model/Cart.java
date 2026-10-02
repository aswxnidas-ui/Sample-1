package com.smartcanteen.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a student's shopping cart holding a list of CartItems.
 * Demonstrates Collections (ArrayList), Encapsulation, and integration contracts with Order module.
 */
public class Cart {

    private Student student;
    private List<CartItem> items;

    /**
     * Default constructor.
     */
    public Cart() {
        this.items = new ArrayList<>();
    }

    /**
     * Constructor associating a cart with a specific student.
     */
    public Cart(Student student) {
        this.student = student;
        this.items = new ArrayList<>();
    }

    /**
     * Adds a food item with the given quantity to the cart.
     * Enforces that the item is available and the combined quantity does not exceed stock.
     *
     * @param item     the FoodItem to add
     * @param quantity number of units (must be > 0)
     * @return true if added successfully, false if item is null, quantity <= 0, unavailable, or exceeds stock
     */
    public boolean addItem(FoodItem item, int quantity) {
        if (item == null || quantity <= 0) {
            return false;
        }

        if (!item.isAvailable()) {
            return false;
        }

        // Check if item is already in cart
        CartItem existingCartItem = findCartItem(item.getFoodId());
        if (existingCartItem != null) {
            int newTotalQuantity = existingCartItem.getQuantity() + quantity;
            if (!item.hasSufficientStock(newTotalQuantity)) {
                return false;
            }
            existingCartItem.setQuantity(newTotalQuantity);
            return true;
        }

        // New item check
        if (!item.hasSufficientStock(quantity)) {
            return false;
        }

        items.add(new CartItem(item, quantity));
        return true;
    }

    /**
     * Removes a food item from the cart by its food ID.
     *
     * @param foodId ID of the food item to remove
     * @return true if removed, false if not found
     */
    public boolean removeItem(int foodId) {
        return items.removeIf(item -> item.getFoodId() == foodId);
    }

    /**
     * Updates the quantity of a specific food item in the cart.
     * If new quantity is 0, the item is removed.
     * Validates that the new quantity does not exceed the food item's available stock.
     *
     * @param foodId   ID of the food item
     * @param quantity new quantity (>= 0)
     * @return true if updated/removed, false if item not in cart, quantity < 0, or exceeds stock
     */
    public boolean updateQuantity(int foodId, int quantity) {
        if (quantity < 0) {
            return false;
        }

        if (quantity == 0) {
            return removeItem(foodId);
        }

        CartItem item = findCartItem(foodId);
        if (item == null) {
            return false;
        }

        FoodItem food = item.getFoodItem();
        if (food != null && (!food.isAvailable() || !food.hasSufficientStock(quantity))) {
            return false;
        }

        item.setQuantity(quantity);
        return true;
    }

    /**
     * Calculates the total price of all items currently in the cart.
     *
     * @return sum of all item total prices
     */
    public double getSubtotal() {
        double subtotal = 0.0;
        for (CartItem item : items) {
            subtotal += item.getTotalPrice();
        }
        return subtotal;
    }

    /**
     * Checks if the cart contains no items.
     * Used for Empty-Cart protection before checkout.
     *
     * @return true if empty, false otherwise
     */
    public boolean isEmpty() {
        return items.isEmpty();
    }

    /**
     * Returns an unmodifiable list of copies so callers cannot mutate cart items.
     *
     * @return unmodifiable snapshot of CartItems
     */
    public List<CartItem> getItems() {
        List<CartItem> snapshots = new ArrayList<>(items.size());
        for (CartItem item : items) {
            snapshots.add(item.copy());
        }
        return Collections.unmodifiableList(snapshots);
    }

    /**
     * Returns the count of distinct food line items in the cart.
     */
    public int getItemCount() {
        return items.size();
    }

    /**
     * Clears all items from the cart.
     * Called exactly once after an order is successfully created.
     */
    public void clear() {
        items.clear();
    }

    /**
     * Finds a CartItem in the cart by food ID.
     *
     * @param foodId ID to search for
     * @return CartItem if present, or null
     */
    public CartItem findCartItem(int foodId) {
        for (CartItem item : items) {
            if (item.getFoodId() == foodId) {
                return item;
            }
        }
        return null;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    @Override
    public String toString() {
        return "Cart{" +
                "student=" + (student != null ? student.getName() : "None") +
                ", itemsCount=" + items.size() +
                ", subtotal=" + getSubtotal() +
                '}';
    }
}
