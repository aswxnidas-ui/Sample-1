package com.smartcanteen.dao;

import com.smartcanteen.model.FoodItem;
import com.smartcanteen.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for FoodItem persistence and live stock verification.
 * Interacts with the 'food_item' table.
 */
public class FoodItemDAO {

    /**
     * Retrieves all food items from the menu.
     *
     * @return List of all FoodItems
     * @throws SQLException if a database error occurs
     */
    public List<FoodItem> getAllFoodItems() throws SQLException {
        List<FoodItem> items = new ArrayList<>();
        String sql = "SELECT food_id, name, category, price, available, stock_quantity FROM food_item ORDER BY category, name";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                items.add(mapResultSetToFoodItem(rs));
            }
        }
        return items;
    }

    /**
     * Retrieves only items that are marked active and have stock > 0.
     *
     * @return List of available FoodItems
     * @throws SQLException if a database error occurs
     */
    public List<FoodItem> getAvailableFoodItems() throws SQLException {
        List<FoodItem> items = new ArrayList<>();
        String sql = "SELECT food_id, name, category, price, available, stock_quantity " +
                     "FROM food_item " +
                     "WHERE available = TRUE AND stock_quantity > 0 " +
                     "ORDER BY category, name";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                items.add(mapResultSetToFoodItem(rs));
            }
        }
        return items;
    }

    /**
     * Retrieves food items filtered by category.
     *
     * @param category category name (e.g. "Meals", "Snacks", "Beverages")
     * @return List of FoodItems in the given category
     * @throws SQLException if a database error occurs
     */
    public List<FoodItem> getFoodItemsByCategory(String category) throws SQLException {
        List<FoodItem> items = new ArrayList<>();
        String sql = "SELECT food_id, name, category, price, available, stock_quantity " +
                     "FROM food_item " +
                     "WHERE category = ? AND available = TRUE AND stock_quantity > 0 " +
                     "ORDER BY name";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, category);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    items.add(mapResultSetToFoodItem(rs));
                }
            }
        }
        return items;
    }

    /**
     * Searches for food items matching a name keyword.
     *
     * @param keyword search keyword
     * @return List of matching FoodItems
     * @throws SQLException if a database error occurs
     */
    public List<FoodItem> searchFoodItems(String keyword) throws SQLException {
        List<FoodItem> items = new ArrayList<>();
        String sql = "SELECT food_id, name, category, price, available, stock_quantity " +
                     "FROM food_item " +
                     "WHERE name LIKE ? AND available = TRUE AND stock_quantity > 0 " +
                     "ORDER BY name";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + keyword + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    items.add(mapResultSetToFoodItem(rs));
                }
            }
        }
        return items;
    }

    /**
     * Retrieves a single FoodItem by its unique ID.
     *
     * @param foodId ID of the food item
     * @return FoodItem if found, null otherwise
     * @throws SQLException if a database error occurs
     */
    public FoodItem getFoodItemById(int foodId) throws SQLException {
        String sql = "SELECT food_id, name, category, price, available, stock_quantity FROM food_item WHERE food_id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, foodId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToFoodItem(rs);
                }
            }
        }
        return null;
    }

    /**
     * Updates the stock quantity and availability flag of a food item.
     *
     * @param foodId   food ID
     * @param newStock new inventory count
     * @return true if updated, false otherwise
     * @throws SQLException if a database error occurs
     */
    public boolean updateStock(int foodId, int newStock) throws SQLException {
        String sql = "UPDATE food_item SET stock_quantity = ?, available = ? WHERE food_id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, newStock);
            stmt.setBoolean(2, newStock > 0);
            stmt.setInt(3, foodId);

            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Atomically deducts stock for an ordered item.
     *
     * @param foodId   ID of the food item
     * @param quantity number of units ordered
     * @return true if stock was deducted, false if insufficient stock
     * @throws SQLException if a database error occurs
     */
    public boolean deductStock(int foodId, int quantity) throws SQLException {
        String sql = "UPDATE food_item " +
                     "SET stock_quantity = stock_quantity - ?, " +
                     "    available = CASE WHEN (stock_quantity - ?) > 0 THEN TRUE ELSE FALSE END " +
                     "WHERE food_id = ? AND stock_quantity >= ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, quantity);
            stmt.setInt(2, quantity);
            stmt.setInt(3, foodId);
            stmt.setInt(4, quantity);

            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Helper method to convert a ResultSet row into a FoodItem object.
     */
    private FoodItem mapResultSetToFoodItem(ResultSet rs) throws SQLException {
        return new FoodItem(
                rs.getInt("food_id"),
                rs.getString("name"),
                rs.getString("category"),
                rs.getDouble("price"),
                rs.getBoolean("available"),
                rs.getInt("stock_quantity")
        );
    }
}
