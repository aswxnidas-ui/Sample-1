package com.smartcanteen.model;

/**
 * Represents a food item on the canteen menu.
 * Shared contract between Aswani (Menu/Cart), Neeraja (Admin/Inventory), and Shreya (Order).
 */
public class FoodItem {

    private int foodId;
    private String name;
    private String category;
    private double price;
    private boolean available;
    private int stockQuantity;

    /**
     * Default constructor.
     */
    public FoodItem() {
    }

    /**
     * Constructor for creating a new food item before ID is assigned by the database.
     */
    public FoodItem(String name, String category, double price, boolean available, int stockQuantity) {
        this.name = name;
        this.category = category;
        this.price = price;
        this.available = available;
        this.stockQuantity = stockQuantity;
    }

    /**
     * Full constructor for retrieving an existing food item from the database.
     */
    public FoodItem(int foodId, String name, String category, double price, boolean available, int stockQuantity) {
        this.foodId = foodId;
        this.name = name;
        this.category = category;
        this.price = price;
        this.available = available;
        this.stockQuantity = stockQuantity;
    }

    // Getters and Setters (Encapsulation)

    public int getFoodId() {
        return foodId;
    }

    public void setFoodId(int foodId) {
        this.foodId = foodId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public boolean isAvailable() {
        return available && stockQuantity > 0;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    /**
     * Checks if there is enough stock available for the requested quantity.
     *
     * @param requestedQty number of units requested
     * @return true if item is marked available and stockQuantity >= requestedQty
     */
    public boolean hasSufficientStock(int requestedQty) {
        return this.available && requestedQty > 0 && this.stockQuantity >= requestedQty;
    }

    /**
     * Reduces the in-memory stock quantity when an item is ordered.
     *
     * @param qty quantity to subtract
     */
    public void reduceStock(int qty) {
        if (qty > 0 && this.stockQuantity >= qty) {
            this.stockQuantity -= qty;
            if (this.stockQuantity == 0) {
                this.available = false;
            }
        }
    }

    /**
     * Restores stock quantity (e.g. if order is cancelled).
     *
     * @param qty quantity to restore
     */
    public void restoreStock(int qty) {
        if (qty > 0) {
            this.stockQuantity += qty;
            this.available = true;
        }
    }

    @Override
    public String toString() {
        return "FoodItem{" +
                "foodId=" + foodId +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", price=" + price +
                ", available=" + available +
                ", stockQuantity=" + stockQuantity +
                '}';
    }
}
