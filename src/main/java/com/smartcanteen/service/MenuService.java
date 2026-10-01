package com.smartcanteen.service;

import com.smartcanteen.dao.FoodItemDAO;
import com.smartcanteen.model.FoodItem;
import com.smartcanteen.model.Menu;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service managing Canteen Food Menu retrieval, searching, category filtering,
 * and live inventory stock availability checks.
 */
public class MenuService {

    private final FoodItemDAO foodItemDAO;
    private final Menu cachedMenu;

    /**
     * Default constructor initializing FoodItemDAO and empty Menu cache.
     */
    public MenuService() {
        this.foodItemDAO = new FoodItemDAO();
        this.cachedMenu = new Menu();
    }

    /**
     * Constructor allowing dependency injection of FoodItemDAO.
     */
    public MenuService(FoodItemDAO foodItemDAO) {
        this.foodItemDAO = (foodItemDAO != null) ? foodItemDAO : new FoodItemDAO();
        this.cachedMenu = new Menu();
    }

    /**
     * Loads the latest food items from the database into the in-memory Menu cache.
     *
     * @return populated Menu object
     */
    public Menu loadMenu() {
        try {
            List<FoodItem> items = foodItemDAO.getAllFoodItems();
            cachedMenu.setItems(items);
        } catch (SQLException e) {
            System.err.println("Failed to load menu from database: " + e.getMessage());
        }
        return cachedMenu;
    }

    /**
     * Retrieves all food items that are currently active and in-stock.
     *
     * @return List of available FoodItems
     */
    public List<FoodItem> getAvailableMenu() {
        try {
            return foodItemDAO.getAvailableFoodItems();
        } catch (SQLException e) {
            System.err.println("Error fetching available menu: " + e.getMessage());
            return cachedMenu.getAvailableOnly();
        }
    }

    /**
     * Searches for food items matching a name keyword.
     *
     * @param query search query
     * @return List of matching FoodItems
     */
    public List<FoodItem> searchMenu(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAvailableMenu();
        }
        try {
            return foodItemDAO.searchFoodItems(query.trim());
        } catch (SQLException e) {
            System.err.println("Error searching menu in database, falling back to cache: " + e.getMessage());
            return cachedMenu.search(query);
        }
    }

    /**
     * Overloaded search: filters menu by both search keyword and category.
     * Demonstrates Method Overloading (Polymorphism).
     *
     * @param query    search query
     * @param category category name
     * @return List of matching FoodItems
     */
    public List<FoodItem> searchMenu(String query, String category) {
        if (category == null || category.equalsIgnoreCase("ALL")) {
            return searchMenu(query);
        }
        List<FoodItem> categoryItems = filterByCategory(category);
        if (query == null || query.trim().isEmpty()) {
            return categoryItems;
        }

        List<FoodItem> filtered = new ArrayList<>();
        String normalized = query.trim().toLowerCase();
        for (FoodItem item : categoryItems) {
            if (item.getName() != null && item.getName().toLowerCase().contains(normalized)) {
                filtered.add(item);
            }
        }
        return filtered;
    }

    /**
     * Filters food items by specific category (e.g. "Meals", "Snacks", "Beverages").
     *
     * @param category category name
     * @return List of FoodItems in the category
     */
    public List<FoodItem> filterByCategory(String category) {
        if (category == null || category.trim().isEmpty() || category.equalsIgnoreCase("ALL")) {
            return getAvailableMenu();
        }
        try {
            return foodItemDAO.getFoodItemsByCategory(category.trim());
        } catch (SQLException e) {
            System.err.println("Error filtering category in database: " + e.getMessage());
            return cachedMenu.getByCategory(category);
        }
    }

    /**
     * Retrieves a single FoodItem by ID.
     *
     * @param foodId ID of food item
     * @return FoodItem or null if not found
     */
    public FoodItem getFoodItemById(int foodId) {
        try {
            return foodItemDAO.getFoodItemById(foodId);
        } catch (SQLException e) {
            System.err.println("Error fetching food item by ID: " + e.getMessage());
            return cachedMenu.findById(foodId);
        }
    }

    /**
     * Checks if a food item is available with the requested quantity in stock.
     * Fulfills Requirement 7 (Food availability checking) and Neeraja inventory integration.
     *
     * @param foodId            ID of the food item
     * @param requestedQuantity quantity desired
     * @return true if item is available and stock >= requestedQuantity
     */
    public boolean isFoodAvailable(int foodId, int requestedQuantity) {
        if (requestedQuantity <= 0) {
            return false;
        }
        FoodItem item = getFoodItemById(foodId);
        return item != null && item.hasSufficientStock(requestedQuantity);
    }

    /**
     * Returns the in-memory cached Menu.
     */
    public Menu getCachedMenu() {
        return cachedMenu;
    }
}
