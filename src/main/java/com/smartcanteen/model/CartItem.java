package com.smartcanteen.model;

/**
 * Represents a single line-item in a student's Cart.
 * Captures a reference to the FoodItem, the requested quantity, and an immutable
 * unit price snapshot so subsequent menu price changes do not distort the active cart.
 */
public class CartItem {

    private FoodItem foodItem;
    private int quantity;
    private double unitPrice; // Snapshot of price at time of adding

    /**
     * Constructs a CartItem capturing the current food price as the unit price snapshot.
     *
     * @param foodItem the food item to add
     * @param quantity number of units (must be > 0)
     */
    public CartItem(FoodItem foodItem, int quantity) {
        if (foodItem == null) {
            throw new IllegalArgumentException("FoodItem cannot be null");
        }
        this.foodItem = foodItem;
        this.quantity = quantity;
        this.unitPrice = foodItem.getPrice();
    }

    /**
     * Constructor allowing explicit snapshot price specification.
     */
    public CartItem(FoodItem foodItem, int quantity, double unitPrice) {
        if (foodItem == null) {
            throw new IllegalArgumentException("FoodItem cannot be null");
        }
        this.foodItem = foodItem;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    // Getters and Setters

    public FoodItem getFoodItem() {
        return foodItem;
    }

    public int getFoodId() {
        return foodItem != null ? foodItem.getFoodId() : 0;
    }

    public String getFoodName() {
        return foodItem != null ? foodItem.getName() : "";
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        if (quantity > 0) {
            this.quantity = quantity;
        }
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    /**
     * Calculates the subtotal for this specific cart item.
     *
     * @return quantity * unitPrice
     */
    public double getTotalPrice() {
        return this.quantity * this.unitPrice;
    }

    @Override
    public String toString() {
        return "CartItem{" +
                "foodId=" + getFoodId() +
                ", name='" + getFoodName() + '\'' +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                ", totalPrice=" + getTotalPrice() +
                '}';
    }
}
