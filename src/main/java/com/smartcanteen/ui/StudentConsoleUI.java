package com.smartcanteen.ui;

import com.smartcanteen.exception.AuthenticationException;
import com.smartcanteen.exception.InvalidCartOperationException;
import com.smartcanteen.exception.OutOfStockException;
import com.smartcanteen.model.CartItem;
import com.smartcanteen.model.FoodItem;
import com.smartcanteen.model.Student;
import com.smartcanteen.service.AuthService;
import com.smartcanteen.service.CartService;
import com.smartcanteen.service.MenuService;

import java.util.List;
import java.util.Scanner;

/**
 * Interactive Console Interface for Aswani's Student / User / Menu / Cart Module.
 * Allows live demonstration and testing of all 16 module requirements.
 */
public class StudentConsoleUI {

    private final AuthService authService;
    private final MenuService menuService;
    private final CartService cartService;
    private final Scanner scanner;

    public StudentConsoleUI() {
        this.authService = new AuthService();
        this.menuService = new MenuService();
        this.cartService = new CartService(null, this.menuService);
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        System.out.println("====================================================");
        System.out.println(" 🍱 WELCOME TO SMART CANTEEN MANAGEMENT SYSTEM 🍱 ");
        System.out.println("   [ASWANI MODULE — STUDENT / MENU / CART HARNESS]   ");
        System.out.println("====================================================");

        boolean running = true;
        while (running) {
            if (!authService.isLoggedIn()) {
                running = showGuestMenu();
            } else {
                running = showStudentDashboard();
            }
        }
        System.out.println("\n👋 Thank you for using Smart Canteen System. Goodbye!");
    }

    private boolean showGuestMenu() {
        System.out.println("\n--- MAIN MENU ---");
        System.out.println("1. Student Login");
        System.out.println("2. Student Registration");
        System.out.println("3. Browse Menu (Guest View)");
        System.out.println("4. Exit");
        System.out.print("👉 Choose an option (1-4): ");

        String choice = scanner.nextLine().trim();
        switch (choice) {
            case "1":
                handleLogin();
                break;
            case "2":
                handleRegistration();
                break;
            case "3":
                displayMenuTable(menuService.getAvailableMenu());
                break;
            case "4":
                return false;
            default:
                System.out.println("❌ Invalid option. Please enter 1, 2, 3, or 4.");
        }
        return true;
    }

    private boolean showStudentDashboard() {
        try {
            Student currentStudent = authService.getCurrentUser();
            System.out.println("\n====================================================");
            System.out.println(" 🎓 STUDENT DASHBOARD — " + currentStudent.getName().toUpperCase());
            System.out.printf(" 💳 Wallet Balance: $%.2f | Cart Items: %d\n",
                    currentStudent.getWalletBalance(), cartService.getActiveCart().getItemCount());
            System.out.println("====================================================");
            System.out.println("1. View Full Menu");
            System.out.println("2. Search Food by Name");
            System.out.println("3. Filter Menu by Category");
            System.out.println("4. Add Item to Cart");
            System.out.println("5. View Cart & Subtotal");
            System.out.println("6. Update Cart Item Quantity");
            System.out.println("7. Remove Item from Cart");
            System.out.println("8. Proceed to Order Checkout (Cart → Order Integration)");
            System.out.println("9. View My Profile");
            System.out.println("10. Logout");
            System.out.print("👉 Choose an option (1-10): ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    displayMenuTable(menuService.getAvailableMenu());
                    break;
                case "2":
                    handleFoodSearch();
                    break;
                case "3":
                    handleCategoryFilter();
                    break;
                case "4":
                    handleAddToCart();
                    break;
                case "5":
                    displayCart();
                    break;
                case "6":
                    handleUpdateQuantity();
                    break;
                case "7":
                    handleRemoveFromCart();
                    break;
                case "8":
                    handleCheckoutIntegration();
                    break;
                case "9":
                    displayProfile(currentStudent);
                    break;
                case "10":
                    authService.logout();
                    cartService.clearCartAfterOrder();
                    System.out.println("🔒 Logged out successfully. Session cleared.");
                    break;
                default:
                    System.out.println("❌ Invalid option. Please choose between 1 and 10.");
            }
        } catch (AuthenticationException e) {
            System.out.println("⚠️ Session error: " + e.getMessage());
            authService.logout();
        }
        return true;
    }

    private void handleLogin() {
        System.out.println("\n--- STUDENT LOGIN ---");
        System.out.print("Enter Email: ");
        String email = scanner.nextLine().trim();
        System.out.print("Enter Password: ");
        String password = scanner.nextLine().trim();

        try {
            Student student = authService.login(email, password);
            cartService.setStudent(student);
            System.out.println("✅ Login successful! Welcome, " + student.getName() + ".");
        } catch (AuthenticationException e) {
            System.out.println("❌ Login Failed: " + e.getMessage());
        }
    }

    private void handleRegistration() {
        System.out.println("\n--- STUDENT REGISTRATION ---");
        System.out.print("Full Name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Email: ");
        String email = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();
        System.out.print("Roll Number (e.g. CS101): ");
        String rollNumber = scanner.nextLine().trim();
        System.out.print("Department (e.g. Computer Science): ");
        String department = scanner.nextLine().trim();

        Student student = new Student(name, email, password, rollNumber, department);
        try {
            boolean success = authService.register(student);
            if (success) {
                System.out.println("🎉 Registration successful! You can now log in.");
            } else {
                System.out.println("❌ Registration failed. Please check inputs.");
            }
        } catch (AuthenticationException e) {
            System.out.println("❌ Registration Error: " + e.getMessage());
        }
    }

    private void displayMenuTable(List<FoodItem> items) {
        System.out.println("\n------------------------------------------------------------------");
        System.out.printf("%-6s | %-20s | %-12s | %-8s | %-6s\n", "ID", "Name", "Category", "Price", "Stock");
        System.out.println("------------------------------------------------------------------");
        if (items.isEmpty()) {
            System.out.println("   (No food items available matching your criteria)");
        } else {
            for (FoodItem item : items) {
                System.out.printf("%-6d | %-20s | %-12s | $%-7.2f | %-6d\n",
                        item.getFoodId(), item.getName(), item.getCategory(), item.getPrice(), item.getStockQuantity());
            }
        }
        System.out.println("------------------------------------------------------------------");
    }

    private void handleFoodSearch() {
        System.out.print("\nEnter keyword to search (e.g. 'Coffee', 'Rice'): ");
        String keyword = scanner.nextLine().trim();
        List<FoodItem> results = menuService.searchMenu(keyword);
        System.out.println("\n🔍 Search results for: '" + keyword + "' (" + results.size() + " found)");
        displayMenuTable(results);
    }

    private void handleCategoryFilter() {
        System.out.println("\nCategories: [Meals, Snacks, Beverages, Bakery, ALL]");
        System.out.print("Enter category to filter: ");
        String category = scanner.nextLine().trim();
        List<FoodItem> results = menuService.filterByCategory(category);
        System.out.println("\n📂 Items in category: '" + category + "' (" + results.size() + " found)");
        displayMenuTable(results);
    }

    private void handleAddToCart() {
        System.out.print("\nEnter Food Item ID to add: ");
        int foodId = parseIntSafe(scanner.nextLine().trim());
        if (foodId <= 0) {
            System.out.println("❌ Invalid Food ID.");
            return;
        }

        System.out.print("Enter Quantity: ");
        int quantity = parseIntSafe(scanner.nextLine().trim());
        if (quantity <= 0) {
            System.out.println("❌ Quantity must be a positive integer.");
            return;
        }

        try {
            cartService.addToCart(foodId, quantity);
            System.out.println("✅ Added to cart! Subtotal: $" + String.format("%.2f", cartService.calculateSubtotal()));
        } catch (OutOfStockException e) {
            System.out.println("⚠️ Out of Stock Error: " + e.getMessage());
        } catch (InvalidCartOperationException e) {
            System.out.println("⚠️ Cart Error: " + e.getMessage());
        }
    }

    private void displayCart() {
        System.out.println("\n----------------- 🛒 YOUR SHOPPING CART -----------------");
        List<CartItem> items = cartService.getActiveCart().getItems();
        if (items.isEmpty()) {
            System.out.println("   (Your cart is currently empty)");
        } else {
            System.out.printf("%-6s | %-20s | %-5s | %-10s | %-10s\n", "ID", "Name", "Qty", "Unit Price", "Total");
            System.out.println("---------------------------------------------------------");
            for (CartItem item : items) {
                System.out.printf("%-6d | %-20s | %-5d | $%-9.2f | $%-9.2f\n",
                        item.getFoodId(), item.getFoodName(), item.getQuantity(), item.getUnitPrice(), item.getTotalPrice());
            }
            System.out.println("---------------------------------------------------------");
            System.out.printf(" 💰 SUBTOTAL: $%.2f\n", cartService.calculateSubtotal());
        }
        System.out.println("---------------------------------------------------------");
    }

    private void handleUpdateQuantity() {
        System.out.print("\nEnter Food Item ID to update: ");
        int foodId = parseIntSafe(scanner.nextLine().trim());
        System.out.print("Enter New Quantity (Enter 0 to remove): ");
        int newQty = parseIntSafe(scanner.nextLine().trim());

        try {
            cartService.updateCartItemQuantity(foodId, newQty);
            System.out.println("✅ Cart updated successfully! New Subtotal: $" + String.format("%.2f", cartService.calculateSubtotal()));
        } catch (OutOfStockException | InvalidCartOperationException e) {
            System.out.println("⚠️ Update Error: " + e.getMessage());
        }
    }

    private void handleRemoveFromCart() {
        System.out.print("\nEnter Food Item ID to remove: ");
        int foodId = parseIntSafe(scanner.nextLine().trim());
        try {
            cartService.removeFromCart(foodId);
            System.out.println("✅ Item removed from cart. New Subtotal: $" + String.format("%.2f", cartService.calculateSubtotal()));
        } catch (InvalidCartOperationException e) {
            System.out.println("⚠️ Remove Error: " + e.getMessage());
        }
    }

    private void handleCheckoutIntegration() {
        System.out.println("\n--- SIMULATING CART → ORDER INTEGRATION (SHREYA'S MODULE) ---");
        try {
            // 1. Validate and fetch safe snapshot
            List<CartItem> orderSnapshot = cartService.getCartSnapshotForOrder();
            double total = cartService.calculateSubtotal();

            System.out.println("📦 Order Snapshot successfully created for Shreya's Order Service:");
            for (CartItem item : orderSnapshot) {
                System.out.printf("  • %s x %d @ $%.2f each = $%.2f\n",
                        item.getFoodName(), item.getQuantity(), item.getUnitPrice(), item.getTotalPrice());
            }
            System.out.printf("💵 Total Billed Amount: $%.2f\n", total);

            // 2. Clear cart exactly once
            cartService.clearCartAfterOrder();
            System.out.println("✨ Cart cleared exactly once after successful order creation!");

        } catch (InvalidCartOperationException e) {
            System.out.println("❌ Checkout Blocked: " + e.getMessage());
        }
    }

    private void displayProfile(Student student) {
        System.out.println("\n--- 👤 STUDENT PROFILE ---");
        System.out.println("User ID: " + student.getUserId());
        System.out.println("Name: " + student.getName());
        System.out.println("Email: " + student.getEmail());
        System.out.println("Roll Number: " + student.getRollNumber());
        System.out.println("Department: " + student.getDepartment());
        System.out.printf("Wallet Balance: $%.2f\n", student.getWalletBalance());
    }

    private int parseIntSafe(String input) {
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public static void main(String[] args) {
        new StudentConsoleUI().start();
    }
}
