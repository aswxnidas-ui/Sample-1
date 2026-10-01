package com.smartcanteen.exception;

/**
 * Custom checked exception thrown when a student attempts to add or update
 * a food item that is unavailable or has insufficient stock.
 */
public class OutOfStockException extends Exception {

    private int foodId;
    private int requestedQuantity;
    private int availableStock;

    public OutOfStockException(String message) {
        super(message);
    }

    public OutOfStockException(int foodId, int requestedQuantity, int availableStock) {
        super("Requested quantity (" + requestedQuantity + ") exceeds available stock (" + availableStock + ") for food ID: " + foodId);
        this.foodId = foodId;
        this.requestedQuantity = requestedQuantity;
        this.availableStock = availableStock;
    }

    public int getFoodId() {
        return foodId;
    }

    public int getRequestedQuantity() {
        return requestedQuantity;
    }

    public int getAvailableStock() {
        return availableStock;
    }
}
