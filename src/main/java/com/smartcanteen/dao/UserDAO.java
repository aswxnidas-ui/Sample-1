package com.smartcanteen.dao;

import com.smartcanteen.model.Student;
import com.smartcanteen.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Data Access Object (DAO) for Student and User database persistence.
 * Maps to the unified 'users' table schema:
 * users(user_id, name, email, password, role, roll_number, department, wallet_balance).
 */
public class UserDAO {

    /**
     * Authenticates a student using email and password.
     *
     * @param email    student email/username
     * @param password student account password
     * @return populated Student object if authenticated, or null if credentials do not match
     * @throws SQLException if a database error occurs
     */
    public Student authenticateStudent(String email, String password) throws SQLException {
        String sql = "SELECT user_id, name, email, password, roll_number, department, wallet_balance " +
                     "FROM users " +
                     "WHERE email = ? AND password = ? AND role = 'STUDENT'";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Student(
                            rs.getInt("user_id"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("password"),
                            rs.getString("roll_number"),
                            rs.getString("department"),
                            rs.getDouble("wallet_balance")
                    );
                }
            }
        }
        return null;
    }

    /**
     * Registers a new student in the 'users' table.
     *
     * @param student student to register
     * @return true if registration succeeded, false otherwise
     * @throws SQLException if a database error occurs
     */
    public boolean registerStudent(Student student) throws SQLException {
        if (student == null) {
            return false;
        }

        String sql = "INSERT INTO users (name, email, password, role, roll_number, department, wallet_balance) " +
                     "VALUES (?, ?, ?, 'STUDENT', ?, ?, ?)";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, student.getName());
            stmt.setString(2, student.getEmail());
            stmt.setString(3, student.getPassword());
            stmt.setString(4, student.getRollNumber());
            stmt.setString(5, student.getDepartment());
            stmt.setDouble(6, student.getWalletBalance());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        student.setUserId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Checks whether an email address is already registered in the 'users' table.
     *
     * @param email email to verify
     * @return true if email exists, false otherwise
     * @throws SQLException if a database error occurs
     */
    public boolean isEmailTaken(String email) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE email = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Retrieves a student by their unique user ID.
     *
     * @param userId user ID to search
     * @return Student if found, null otherwise
     * @throws SQLException if a database error occurs
     */
    public Student getStudentById(int userId) throws SQLException {
        String sql = "SELECT user_id, name, email, password, roll_number, department, wallet_balance " +
                     "FROM users " +
                     "WHERE user_id = ? AND role = 'STUDENT'";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Student(
                            rs.getInt("user_id"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("password"),
                            rs.getString("roll_number"),
                            rs.getString("department"),
                            rs.getDouble("wallet_balance")
                    );
                }
            }
        }
        return null;
    }

    /**
     * Updates the wallet balance of a student in the 'users' table.
     *
     * @param userId     student's user ID
     * @param newBalance updated balance
     * @return true if updated, false otherwise
     * @throws SQLException if a database error occurs
     */
    public boolean updateStudentWallet(int userId, double newBalance) throws SQLException {
        String sql = "UPDATE users SET wallet_balance = ? WHERE user_id = ? AND role = 'STUDENT'";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, newBalance);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        }
    }
}
