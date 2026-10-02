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
 * User credentials are stored in 'users'; student details are stored in 'students'.
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
        String sql = "SELECT u.user_id, u.name, u.email, u.password, " +
                     "s.roll_number, s.department, s.wallet_balance " +
                     "FROM users u " +
                     "JOIN students s ON s.student_id = u.user_id " +
                     "WHERE u.email = ? AND u.password = ? AND u.role = 'STUDENT'";

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

        try (Connection conn = DatabaseUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                String userSql = "INSERT INTO users (name, email, password, role) " +
                                 "VALUES (?, ?, ?, 'STUDENT')";
                int userId;
                try (PreparedStatement userStmt = conn.prepareStatement(userSql, Statement.RETURN_GENERATED_KEYS)) {
                    userStmt.setString(1, student.getName());
                    userStmt.setString(2, student.getEmail());
                    userStmt.setString(3, student.getPassword());
                    if (userStmt.executeUpdate() != 1) {
                        conn.rollback();
                        return false;
                    }

                    try (ResultSet generatedKeys = userStmt.getGeneratedKeys()) {
                        if (!generatedKeys.next()) {
                            conn.rollback();
                            return false;
                        }
                        userId = generatedKeys.getInt(1);
                    }
                }

                String studentSql = "INSERT INTO students " +
                                    "(student_id, roll_number, department, wallet_balance) " +
                                    "VALUES (?, ?, ?, ?)";
                try (PreparedStatement studentStmt = conn.prepareStatement(studentSql)) {
                    studentStmt.setInt(1, userId);
                    studentStmt.setString(2, student.getRollNumber());
                    studentStmt.setString(3, student.getDepartment());
                    studentStmt.setDouble(4, student.getWalletBalance());
                    if (studentStmt.executeUpdate() != 1) {
                        conn.rollback();
                        return false;
                    }
                }

                conn.commit();
                student.setUserId(userId);
                return true;
            } catch (SQLException | RuntimeException e) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackException) {
                    e.addSuppressed(rollbackException);
                }
                throw e;
            }
        }
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
        String sql = "SELECT u.user_id, u.name, u.email, u.password, " +
                     "s.roll_number, s.department, s.wallet_balance " +
                     "FROM users u " +
                     "JOIN students s ON s.student_id = u.user_id " +
                     "WHERE u.user_id = ? AND u.role = 'STUDENT'";

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
    * Updates the wallet balance of a student in the 'students' table.
     *
     * @param userId     student's user ID
     * @param newBalance updated balance
     * @return true if updated, false otherwise
     * @throws SQLException if a database error occurs
     */
    public boolean updateStudentWallet(int userId, double newBalance) throws SQLException {
        String sql = "UPDATE students SET wallet_balance = ? WHERE student_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, newBalance);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        }
    }
}
