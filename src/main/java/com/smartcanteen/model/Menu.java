package com.smartcanteen.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * In-memory domain model representing the Canteen Food Menu.
 * Demonstrates Collections (ArrayList), Encapsulation, and Method Overloading (search).
 */
public class Menu {

    private List<FoodItem> items;

    /**
     * Default constructor initializing an empty menu.
     */
    public Menu() {
        this.items = new ArrayList<>();
    }

    /**
     * Constructor initializing menu with a pre-existing list of items.
     *
     * @param items initial list of food items
     */
    public Menu(List<FoodItem> items) {
        this.items = (items != null) ? new ArrayList<>(items) : new ArrayList<>();
    }

    /**
     * Returns an unmodifiable list of all food items in the menu.
     *
     * @return unmodifiable List of FoodItem
     */
    public List<FoodItem> getAllItems() {
        return Collections.unmodifiableList(items);
    }

    /**
     * Searches for food items whose name matches the given query (case-insensitive).
     *
     * @param query search keyword (e.g., "biryani", "tea")
     * @return List of matching FoodItems
     */
    public List<FoodItem> search(String query) {
        List<FoodItem> results = new ArrayList<>();
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>(items);
        }

        String normalized = query.trim().toLowerCase(Locale.ROOT);
        for (FoodItem item : items) {
            if (item.getName() != null && item.getName().toLowerCase(Locale.ROOT).contains(normalized)) {
                results.add(item);
            }
        }
        return results;
    }

    /**
     * Overloaded search method: filters menu by both search keyword and category.
     * Demonstrates Method Overloading (Compile-Time Polymorphism).
     *
     * @param query    search keyword
     * @param category category name (e.g., "Snacks", "Meals", "Beverages")
     * @return List of matching FoodItems
     */
    public List<FoodItem> search(String query, String category) {
        List<FoodItem> results = new ArrayList<>();
        String normalizedQuery = (query != null) ? query.trim().toLowerCase(Locale.ROOT) : "";
        String normalizedCategory = (category != null) ? category.trim().toLowerCase(Locale.ROOT) : "";

        for (FoodItem item : items) {
            boolean matchesQuery = normalizedQuery.isEmpty() ||
                    (item.getName() != null && item.getName().toLowerCase(Locale.ROOT).contains(normalizedQuery));
            boolean matchesCategory = normalizedCategory.isEmpty() ||
                    (item.getCategory() != null && item.getCategory().toLowerCase(Locale.ROOT).equalsIgnoreCase(normalizedCategory));

            if (matchesQuery && matchesCategory) {
                results.add(item);
            }
        }
        return results;
    }

    /**
     * Overloaded search method: finds all food items priced up to a specified maximum budget.
     * Demonstrates Method Overloading.
     *
     * @param maxPrice maximum affordable price
     * @return List of matching FoodItems within budget
     */
    public List<FoodItem> search(double maxPrice) {
        List<FoodItem> results = new ArrayList<>();
        for (FoodItem item : items) {
            if (item.getPrice() <= maxPrice) {
                results.add(item);
            }
        }
        return results;
    }

    /**
     * Filters food items belonging to a specific category.
     *
     * @param category category name (case-insensitive)
     * @return List of FoodItems in the category
     */
    public List<FoodItem> getByCategory(String category) {
        List<FoodItem> results = new ArrayList<>();
        if (category == null || category.trim().isEmpty() || category.equalsIgnoreCase("ALL")) {
            return new ArrayList<>(items);
        }

        for (FoodItem item : items) {
            if (item.getCategory() != null && item.getCategory().equalsIgnoreCase(category.trim())) {
                results.add(item);
            }
        }
        return results;
    }

    /**
     * Returns only items that are currently marked available and in stock.
     *
     * @return List of in-stock FoodItems
     */
    public List<FoodItem> getAvailableOnly() {
        List<FoodItem> results = new ArrayList<>();
        for (FoodItem item : items) {
            if (item.isAvailable()) {
                results.add(item);
            }
        }
        return results;
    }

    /**
     * Finds a single food item by its unique ID.
     *
     * @param foodId ID to look up
     * @return matching FoodItem, or null if not found
     */
    public FoodItem findById(int foodId) {
        for (FoodItem item : items) {
            if (item.getFoodId() == foodId) {
                return item;
            }
        }
        return null;
    }

    /**
     * Adds a food item to the menu.
     *
     * @param item FoodItem to add
     */
    public void addItem(FoodItem item) {
        if (item != null) {
            items.add(item);
        }
    }

    /**
     * Removes a food item from the menu by ID.
     *
     * @param foodId ID of the food item to remove
     * @return true if removed, false if not found
     */
    public boolean removeItem(int foodId) {
        return items.removeIf(item -> item.getFoodId() == foodId);
    }

    /**
     * Replaces the entire list of items in the menu.
     *
     * @param items new list of items
     */
    public void setItems(List<FoodItem> items) {
        this.items = (items != null) ? new ArrayList<>(items) : new ArrayList<>();
    }

    /**
     * Returns the total number of items on the menu.
     */
    public int size() {
        return items.size();
    }

    @Override
    public String toString() {
        return "Menu{" +
                "totalItems=" + items.size() +
                '}';
    }
}
