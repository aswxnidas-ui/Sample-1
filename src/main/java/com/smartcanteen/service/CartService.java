package com.smartcanteen.service;

import com.smartcanteen.exception.InvalidCartOperationException;
import com.smartcanteen.exception.OutOfStockException;
import com.smartcanteen.model.Cart;
import com.smartcanteen.model.CartItem;
import com.smartcanteen.model.FoodItem;
import com.smartcanteen.model.Student;

import java.util.List;

/**
 * Service managing shopping cart operations, stock enforcement,
 * empty-cart protection, and integration contracts with Shreya's Order module.
 */
public class CartService {

    private final Cart cart;
    private final MenuService menuService;

    /**
     * Default constructor.
     */
    public CartService() {
        this.cart = new Cart();
        this.menuService = new MenuService();
    }

    /**
     * Constructor allowing association with an active student and MenuService.
     */
    public CartService(Student student, MenuService menuService) {
        this.cart = new Cart(student);
        this.menuService = (menuService != null) ? menuService : new MenuService();
    }

    /**
     * Adds a food item to the cart by its FoodItem object.
     * Enforces non-zero quantity, availability, and stock limits.
     *
     * @param item     the FoodItem to add
     * @param quantity number of units
     * @throws InvalidCartOperationException if quantity <= 0 or item is null
     * @throws OutOfStockException           if item is unavailable or requested units exceed stock
     */
    public void addToCart(FoodItem item, int quantity) throws InvalidCartOperationException, OutOfStockException {
        if (item == null) {
            throw new InvalidCartOperationException("Cannot add a null food item to cart.");
        }
        if (quantity <= 0) {
            throw new InvalidCartOperationException("Quantity must be greater than zero. Received: " + quantity);
        }

        // Live check against inventory
        FoodItem liveItem = menuService.getFoodItemById(item.getFoodId());
        FoodItem itemToCheck = (liveItem != null) ? liveItem : item;

        if (!itemToCheck.isAvailable()) {
            throw new OutOfStockException("Food item '" + itemToCheck.getName() + "' is currently out of stock / unavailable.");
        }

        CartItem existing = cart.findCartItem(itemToCheck.getFoodId());
        int totalRequested = (existing != null) ? existing.getQuantity() + quantity : quantity;

        if (!itemToCheck.hasSufficientStock(totalRequested)) {
            throw new OutOfStockException(itemToCheck.getFoodId(), totalRequested, itemToCheck.getStockQuantity());
        }

        boolean success = cart.addItem(itemToCheck, quantity);
        if (!success) {
            if (!itemToCheck.isAvailable() || !itemToCheck.hasSufficientStock(totalRequested)) {
                throw new OutOfStockException(itemToCheck.getFoodId(), totalRequested, itemToCheck.getStockQuantity());
            } else {
                throw new InvalidCartOperationException("Failed to add item '" + itemToCheck.getName() + "' to cart.");
            }
        }
    }

    /**
     * Adds a food item to the cart by its food ID.
     *
     * @param foodId   ID of the food item
     * @param quantity number of units
     * @throws InvalidCartOperationException if foodId is invalid or quantity <= 0
     * @throws OutOfStockException           if item is unavailable or requested units exceed stock
     */
    public void addToCart(int foodId, int quantity) throws InvalidCartOperationException, OutOfStockException {
        FoodItem item = menuService.getFoodItemById(foodId);
        if (item == null) {
            throw new InvalidCartOperationException("Food item with ID " + foodId + " does not exist.");
        }
        addToCart(item, quantity);
    }

    /**
     * Updates the quantity of an item in the cart.
     * Setting quantity to 0 removes the item.
     *
     * @param foodId   ID of the food item
     * @param quantity new quantity (>= 0)
     * @throws InvalidCartOperationException if item is not in cart or quantity < 0
     * @throws OutOfStockException           if new quantity exceeds available inventory stock
     */
    public void updateCartItemQuantity(int foodId, int quantity) throws InvalidCartOperationException, OutOfStockException {
        if (quantity < 0) {
            throw new InvalidCartOperationException("Quantity cannot be negative: " + quantity);
        }

        CartItem existing = cart.findCartItem(foodId);
        if (existing == null) {
            throw new InvalidCartOperationException("Food item ID " + foodId + " was not found in your cart.");
        }

        if (quantity == 0) {
            cart.removeItem(foodId);
            return;
        }

        FoodItem liveItem = menuService.getFoodItemById(foodId);
        int availableStock = (liveItem != null) ? liveItem.getStockQuantity() : existing.getFoodItem().getStockQuantity();

        if (quantity > availableStock) {
            throw new OutOfStockException(foodId, quantity, availableStock);
        }

        boolean updated = cart.updateQuantity(foodId, quantity);
        if (!updated) {
            throw new InvalidCartOperationException("Failed to update quantity for food ID " + foodId);
        }
    }

    /**
     * Removes an item from the cart.
     *
     * @param foodId ID of the food item to remove
     * @throws InvalidCartOperationException if item is not found in the cart
     */
    public void removeFromCart(int foodId) throws InvalidCartOperationException {
        boolean removed = cart.removeItem(foodId);
        if (!removed) {
            throw new InvalidCartOperationException("Cannot remove item. Food ID " + foodId + " is not in the cart.");
        }
    }

    /**
     * Calculates the subtotal of all items currently in the cart.
     *
     * @return current subtotal
     */
    public double calculateSubtotal() {
        return cart.getSubtotal();
    }

    /**
     * Checks if the cart is ready for checkout.
     * Enforces Empty-Cart protection (Requirement 14).
     *
     * @return true if cart has items and valid subtotal
     * @throws InvalidCartOperationException if cart is empty
     */
    public boolean validateCartForCheckout() throws InvalidCartOperationException {
        if (cart.isEmpty() || cart.getItemCount() == 0) {
            throw new InvalidCartOperationException("Cannot proceed to checkout. Your cart is empty.");
        }
        if (cart.getSubtotal() <= 0.0) {
            throw new InvalidCartOperationException("Invalid cart subtotal. Please verify cart items.");
        }
        return true;
    }

    /**
     * Returns a safe, unmodifiable snapshot of cart items for Shreya's Order module.
     * Requirement 15: Safe price snapshot for order creation.
     *
     * @return List of CartItems
     * @throws InvalidCartOperationException if cart is empty
     */
    public List<CartItem> getCartSnapshotForOrder() throws InvalidCartOperationException {
        validateCartForCheckout();
        return cart.getItems();
    }

    /**
     * Clears all items from the cart.
     * Requirement: Called exactly once after an order is successfully created and saved.
     */
    public void clearCartAfterOrder() {
        cart.clear();
    }

    /**
     * Returns the underlying Cart instance.
     */
    public Cart getActiveCart() {
        return cart;
    }

    /**
     * Associates the cart with a logged-in student.
     */
    public void setStudent(Student student) {
        cart.setStudent(student);
    }
}
