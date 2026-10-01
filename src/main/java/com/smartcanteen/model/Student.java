package com.smartcanteen.model;

/**
 * Entity representing a Student in the canteen system.
 * Inherits common user credentials from User (Inheritance)
 * and maintains student-specific attributes such as rollNumber, department, and wallet balance.
 */
public class Student extends User {

    private String rollNumber;
    private String department;
    private double walletBalance;

    /**
     * Default constructor.
     */
    public Student() {
        super();
        setRole("STUDENT");
    }

    /**
     * Constructor used during student registration.
     */
    public Student(String name, String email, String password, String rollNumber, String department) {
        super(name, email, password, "STUDENT");
        this.rollNumber = rollNumber;
        this.department = department;
        this.walletBalance = 0.0;
    }

    /**
     * Full constructor when fetching an existing student record from the database.
     */
    public Student(int userId, String name, String email, String password, String rollNumber, String department, double walletBalance) {
        super(userId, name, email, password, "STUDENT");
        this.rollNumber = rollNumber;
        this.department = department;
        this.walletBalance = walletBalance;
    }

    // Getters and Setters

    public String getRollNumber() {
        return rollNumber;
    }

    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public double getWalletBalance() {
        return walletBalance;
    }

    public void setWalletBalance(double walletBalance) {
        this.walletBalance = walletBalance;
    }

    /**
     * Adds funds to the student's digital canteen wallet.
     *
     * @param amount positive money amount to deposit
     */
    public void addBalance(double amount) {
        if (amount > 0) {
            this.walletBalance += amount;
        }
    }

    /**
     * Deducts funds from the student's wallet if sufficient balance is available.
     *
     * @param amount purchase total to deduct
     * @return true if deduction succeeded, false if insufficient funds
     */
    public boolean deductBalance(double amount) {
        if (amount > 0 && this.walletBalance >= amount) {
            this.walletBalance -= amount;
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "Student{" +
                "userId=" + getUserId() +
                ", name='" + getName() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", rollNumber='" + rollNumber + '\'' +
                ", department='" + department + '\'' +
                ", walletBalance=" + walletBalance +
                '}';
    }
}
