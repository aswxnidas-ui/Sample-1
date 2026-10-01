package com.smartcanteen.test;

import com.smartcanteen.dao.FoodItemDAO;
import com.smartcanteen.exception.AuthenticationException;
import com.smartcanteen.exception.InvalidCartOperationException;
import com.smartcanteen.exception.OutOfStockException;
import com.smartcanteen.model.Cart;
import com.smartcanteen.model.CartItem;
import com.smartcanteen.model.FoodItem;
import com.smartcanteen.model.Menu;
import com.smartcanteen.model.Student;
import com.smartcanteen.model.User;
import com.smartcanteen.service.AuthService;
import com.smartcanteen.service.CartService;
import com.smartcanteen.service.MenuService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Comprehensive Unit Test Suite for Aswani's Student / User / Menu / Cart Module.
 * Covers all 16 project requirements, integration contracts, and OOP concepts.
 */
public class AswaniModuleTest {

    private Student testStudent;
    private FoodItem meals;
    private FoodItem coffee;
    private FoodItem sandwichOutStock;
    private Menu menu;
    private Cart cart;
    private MenuService testMenuService;

    /**
     * In-memory FoodItemDAO test double demonstrating Polymorphism / Overriding
     * without needing a live MySQL database connection during unit tests.
     */
    private static class MockFoodItemDAO extends FoodItemDAO {
        private final List<FoodItem> mockItems;

        public MockFoodItemDAO(List<FoodItem> mockItems) {
            this.mockItems = mockItems;
        }

        @Override
        public List<FoodItem> getAllFoodItems() {
            return new ArrayList<>(mockItems);
        }

        @Override
        public List<FoodItem> getAvailableFoodItems() {
            List<FoodItem> available = new ArrayList<>();
            for (FoodItem item : mockItems) {
                if (item.isAvailable()) {
                    available.add(item);
                }
            }
            return available;
        }

        @Override
        public List<FoodItem> getFoodItemsByCategory(String category) {
            List<FoodItem> results = new ArrayList<>();
            for (FoodItem item : mockItems) {
                if (item.getCategory() != null && item.getCategory().equalsIgnoreCase(category)) {
                    results.add(item);
                }
            }
            return results;
        }

        @Override
        public List<FoodItem> searchFoodItems(String keyword) {
            List<FoodItem> results = new ArrayList<>();
            for (FoodItem item : mockItems) {
                if (item.getName() != null && item.getName().toLowerCase().contains(keyword.toLowerCase())) {
                    results.add(item);
                }
            }
            return results;
        }

        @Override
        public FoodItem getFoodItemById(int foodId) {
            for (FoodItem item : mockItems) {
                if (item.getFoodId() == foodId) {
                    return item;
                }
            }
            return null;
        }
    }

    @BeforeEach
    public void setUp() {
        testStudent = new Student(1, "Aswani", "aswani@canteen.edu", "pass123", "CS101", "Computer Science", 250.0);

        meals = new FoodItem(101, "Veg Meals", "Meals", 60.0, true, 20);
        coffee = new FoodItem(102, "Filter Coffee", "Beverages", 15.0, true, 30);
        sandwichOutStock = new FoodItem(103, "Club Sandwich", "Snacks", 45.0, false, 0);

        List<FoodItem> items = new ArrayList<>();
        items.add(meals);
        items.add(coffee);
        items.add(sandwichOutStock);

        menu = new Menu(items);
        cart = new Cart(testStudent);
        testMenuService = new MenuService(new MockFoodItemDAO(items));
    }

    @Test
    @DisplayName("Req 1 & 2: User and Student Inheritance & Encapsulation")
    public void testUserAndStudentInheritance() {
        assertTrue(testStudent instanceof User, "Student must be an instance of User (Inheritance)");
        assertEquals("STUDENT", testStudent.getRole());
        assertEquals("CS101", testStudent.getRollNumber());
        assertEquals("Computer Science", testStudent.getDepartment());
        assertEquals(250.0, testStudent.getWalletBalance(), 0.001);

        testStudent.addBalance(50.0);
        assertEquals(300.0, testStudent.getWalletBalance(), 0.001);

        boolean deducted = testStudent.deductBalance(100.0);
        assertTrue(deducted);
        assertEquals(200.0, testStudent.getWalletBalance(), 0.001);

        boolean overDeduct = testStudent.deductBalance(500.0);
        assertFalse(overDeduct, "Cannot deduct more than wallet balance");
        assertEquals(200.0, testStudent.getWalletBalance(), 0.001);
    }

    @Test
    @DisplayName("Req 3: FoodItem Stock and Availability")
    public void testFoodItemStockAndAvailability() {
        assertTrue(meals.isAvailable());
        assertTrue(meals.hasSufficientStock(5));
        assertFalse(meals.hasSufficientStock(25), "Requested quantity exceeds stock");

        assertFalse(sandwichOutStock.isAvailable(), "Unavailable item must return false");
        assertFalse(sandwichOutStock.hasSufficientStock(1));
    }

    @Test
    @DisplayName("Req 4, 5, 6, 7: Menu Display, Search, Category Filter, and Overloading")
    public void testMenuOperationsAndOverloading() {
        // 1. Display All
        assertEquals(3, menu.getAllItems().size());

        // 2. Search by keyword
        List<FoodItem> searchResults = menu.search("coffee");
        assertEquals(1, searchResults.size());
        assertEquals("Filter Coffee", searchResults.get(0).getName());

        // 3. Category filtering
        List<FoodItem> beverageItems = menu.getByCategory("Beverages");
        assertEquals(1, beverageItems.size());
        assertEquals(102, beverageItems.get(0).getFoodId());

        // 4. Overloaded search by name + category
        List<FoodItem> mealsSearch = menu.search("Veg", "Meals");
        assertEquals(1, mealsSearch.size());

        // 5. Overloaded search by max price
        List<FoodItem> affordable = menu.search(20.0);
        assertEquals(1, affordable.size());
        assertEquals("Filter Coffee", affordable.get(0).getName());

        // 6. Available only filter
        assertEquals(2, menu.getAvailableOnly().size());
    }

    @Test
    @DisplayName("Req 8 & 11: Add Food to Cart & Quantity Accumulation")
    public void testCartAddAndQuantityAccumulation() {
        boolean addedFirst = cart.addItem(meals, 2);
        assertTrue(addedFirst);
        assertEquals(1, cart.getItemCount());
        assertEquals(2, cart.findCartItem(101).getQuantity());

        // Add same item again -> increments quantity
        boolean addedSecond = cart.addItem(meals, 3);
        assertTrue(addedSecond);
        assertEquals(1, cart.getItemCount(), "Distinct line items count should still be 1");
        assertEquals(5, cart.findCartItem(101).getQuantity());

        // Attempt to exceed total stock (20)
        boolean exceedStock = cart.addItem(meals, 20);
        assertFalse(exceedStock, "Should reject adding when combined quantity (5 + 20 = 25) > stock (20)");
        assertEquals(5, cart.findCartItem(101).getQuantity());
    }

    @Test
    @DisplayName("Req 9 & 10: Remove Food from Cart & Update Quantity")
    public void testCartRemoveAndUpdateQuantity() {
        cart.addItem(meals, 2);
        cart.addItem(coffee, 4);
        assertEquals(2, cart.getItemCount());

        // Update quantity
        boolean updated = cart.updateQuantity(102, 2);
        assertTrue(updated);
        assertEquals(2, cart.findCartItem(102).getQuantity());

        // Update quantity to 0 removes the item
        boolean updateToZero = cart.updateQuantity(102, 0);
        assertTrue(updateToZero);
        assertNull(cart.findCartItem(102), "Item should be removed when quantity updated to 0");
        assertEquals(1, cart.getItemCount());

        // Direct removal
        boolean removed = cart.removeItem(101);
        assertTrue(removed);
        assertTrue(cart.isEmpty());
    }

    @Test
    @DisplayName("Req 12 & 13: Subtotal Calculation & Price Snapshot Immutability")
    public void testSubtotalAndPriceSnapshot() {
        cart.addItem(meals, 2);  // 2 * 60 = 120
        cart.addItem(coffee, 3); // 3 * 15 = 45
        assertEquals(165.0, cart.getSubtotal(), 0.001);

        // Price snapshot test: modifying food price after adding does NOT distort active CartItem
        CartItem cartCoffee = cart.findCartItem(102);
        assertEquals(15.0, cartCoffee.getUnitPrice(), 0.001);
        coffee.setPrice(25.0); // Price changes in menu/inventory
        assertEquals(15.0, cartCoffee.getUnitPrice(), 0.001, "Snapshot price in CartItem must remain unchanged");
    }

    @Test
    @DisplayName("Req 14: Empty-Cart Protection")
    public void testEmptyCartProtection() {
        CartService cartService = new CartService(testStudent, testMenuService);

        // Validation on empty cart must throw exception
        assertThrows(InvalidCartOperationException.class, () -> {
            cartService.validateCartForCheckout();
        });

        assertThrows(InvalidCartOperationException.class, () -> {
            cartService.getCartSnapshotForOrder();
        });
    }

    @Test
    @DisplayName("Req 14 & 16: Out-of-Stock and Invalid Quantity Exceptions")
    public void testOutOfStockAndInvalidQuantityExceptions() {
        CartService cartService = new CartService(testStudent, testMenuService);

        // Negative / Zero quantity
        assertThrows(InvalidCartOperationException.class, () -> {
            cartService.addToCart(meals, 0);
        });

        assertThrows(InvalidCartOperationException.class, () -> {
            cartService.addToCart(meals, -3);
        });

        // Adding unavailable item
        assertThrows(OutOfStockException.class, () -> {
            cartService.addToCart(sandwichOutStock, 1);
        });

        // Exceeding stock
        assertThrows(OutOfStockException.class, () -> {
            cartService.addToCart(meals, 100);
        });
    }

    @Test
    @DisplayName("Req 2: AuthService Login, Logout, and Session Management")
    public void testAuthServiceSession() {
        AuthService authService = new AuthService();

        assertFalse(authService.isLoggedIn());
        assertThrows(AuthenticationException.class, () -> {
            authService.getCurrentUser();
        });

        // Set session
        authService.setCurrentUser(testStudent);
        assertTrue(authService.isLoggedIn());
        assertDoesNotThrow(() -> {
            assertEquals("Aswani", authService.getCurrentUser().getName());
        });

        // Logout
        authService.logout();
        assertFalse(authService.isLoggedIn());
    }

    @Test
    @DisplayName("Req 15 & 16: Full Cart to Order Integration Lifecycle")
    public void testCartToOrderIntegrationLifecycle() throws Exception {
        CartService cartService = new CartService(testStudent, testMenuService);

        cartService.addToCart(meals, 2);
        cartService.addToCart(coffee, 2);

        // 1. Shreya's Order module requests safe read-only snapshot
        List<CartItem> snapshot = cartService.getCartSnapshotForOrder();
        assertEquals(2, snapshot.size());
        assertEquals(150.0, cartService.calculateSubtotal(), 0.001);

        // 2. Read-only safety: attempts to modify snapshot list directly throws UnsupportedOperationException
        assertThrows(UnsupportedOperationException.class, () -> {
            snapshot.clear();
        });

        // 3. Shreya finalizes order -> calls clear exactly once
        cartService.clearCartAfterOrder();
        assertTrue(cartService.getActiveCart().isEmpty());
        assertEquals(0.0, cartService.calculateSubtotal(), 0.001);
    }

    /**
     * Standalone runner for quick manual execution without IDE runner plugin.
     */
    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("🚀 RUNNING ASWANI MODULE COMPREHENSIVE TEST SUITE");
        System.out.println("=================================================");

        AswaniModuleTest testSuite = new AswaniModuleTest();

        try {
            testSuite.setUp();
            testSuite.testUserAndStudentInheritance();
            System.out.println("✅ PASS: User & Student Inheritance & Wallet");

            testSuite.setUp();
            testSuite.testFoodItemStockAndAvailability();
            System.out.println("✅ PASS: FoodItem Stock & Availability");

            testSuite.setUp();
            testSuite.testMenuOperationsAndOverloading();
            System.out.println("✅ PASS: Menu Display, Search, Filter & Overloading");

            testSuite.setUp();
            testSuite.testCartAddAndQuantityAccumulation();
            System.out.println("✅ PASS: Cart Add Item & Stock Limit");

            testSuite.setUp();
            testSuite.testCartRemoveAndUpdateQuantity();
            System.out.println("✅ PASS: Cart Remove & Update Quantity");

            testSuite.setUp();
            testSuite.testSubtotalAndPriceSnapshot();
            System.out.println("✅ PASS: Subtotal & Price Snapshot Immutability");

            testSuite.setUp();
            testSuite.testEmptyCartProtection();
            System.out.println("✅ PASS: Empty-Cart Protection");

            testSuite.setUp();
            testSuite.testOutOfStockAndInvalidQuantityExceptions();
            System.out.println("✅ PASS: Out-of-Stock & Invalid Quantity Exceptions");

            testSuite.setUp();
            testSuite.testAuthServiceSession();
            System.out.println("✅ PASS: AuthService Session Management");

            testSuite.setUp();
            testSuite.testCartToOrderIntegrationLifecycle();
            System.out.println("✅ PASS: Cart → Order Integration Lifecycle");

            System.out.println("=================================================");
            System.out.println("🎉 ALL 10 TEST SUITES PASSED (100% SUCCESS RATE)!");
            System.out.println("=================================================");
        } catch (Throwable t) {
            System.err.println("❌ TEST FAILED: " + t.getMessage());
            t.printStackTrace();
        }
    }
}
