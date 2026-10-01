package com.smartcanteen.service;

import com.smartcanteen.dao.UserDAO;
import com.smartcanteen.exception.AuthenticationException;
import com.smartcanteen.model.Student;

import java.sql.SQLException;

/**
 * Service managing Student authentication, registration, logout, and active session state.
 * Implements clean business logic and exception handling for user credentials.
 */
public class AuthService {

    private final UserDAO userDAO;
    private Student currentUser; // Active session

    /**
     * Default constructor initializing default UserDAO.
     */
    public AuthService() {
        this.userDAO = new UserDAO();
    }

    /**
     * Constructor allowing dependency injection (useful for unit testing).
     */
    public AuthService(UserDAO userDAO) {
        this.userDAO = (userDAO != null) ? userDAO : new UserDAO();
    }

    /**
     * Authenticates a student with email and password.
     * Sets the active session user upon success.
     *
     * @param email    student email
     * @param password student password
     * @return logged-in Student object
     * @throws AuthenticationException if validation fails or credentials are wrong
     */
    public Student login(String email, String password) throws AuthenticationException {
        if (email == null || email.trim().isEmpty()) {
            throw new AuthenticationException("Email address cannot be empty.");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new AuthenticationException("Password cannot be empty.");
        }

        try {
            Student student = userDAO.authenticateStudent(email.trim(), password);
            if (student == null) {
                throw new AuthenticationException("Invalid email or password. Access denied.");
            }
            this.currentUser = student;
            return this.currentUser;
        } catch (SQLException e) {
            throw new AuthenticationException("Authentication service database error: " + e.getMessage(), e);
        }
    }

    /**
     * Registers a new student account after validating input constraints.
     *
     * @param student student details to register
     * @return true if registration succeeded
     * @throws AuthenticationException if fields are missing or email is already taken
     */
    public boolean register(Student student) throws AuthenticationException {
        if (student == null) {
            throw new AuthenticationException("Student data cannot be null.");
        }
        if (student.getName() == null || student.getName().trim().isEmpty()) {
            throw new AuthenticationException("Student name is required.");
        }
        if (student.getEmail() == null || !isValidEmail(student.getEmail())) {
            throw new AuthenticationException("A valid email address is required.");
        }
        if (student.getPassword() == null || student.getPassword().length() < 4) {
            throw new AuthenticationException("Password must be at least 4 characters long.");
        }
        if (student.getRollNumber() == null || student.getRollNumber().trim().isEmpty()) {
            throw new AuthenticationException("Roll number is required.");
        }

        try {
            if (userDAO.isEmailTaken(student.getEmail().trim())) {
                throw new AuthenticationException("Email address '" + student.getEmail() + "' is already registered.");
            }
            return userDAO.registerStudent(student);
        } catch (SQLException e) {
            throw new AuthenticationException("Failed to register student due to database error: " + e.getMessage(), e);
        }
    }

    /**
     * Clears the current active session.
     */
    public void logout() {
        this.currentUser = null;
    }

    /**
     * Checks if a student is currently logged in.
     *
     * @return true if an active session exists, false otherwise
     */
    public boolean isLoggedIn() {
        return this.currentUser != null;
    }

    /**
     * Returns the currently authenticated Student.
     *
     * @return Student object of active session
     * @throws AuthenticationException if no user is currently logged in
     */
    public Student getCurrentUser() throws AuthenticationException {
        if (this.currentUser == null) {
            throw new AuthenticationException("No active student session. Please log in first.");
        }
        return this.currentUser;
    }

    /**
     * Manually sets the active session user (used for testing or pre-authenticated flows).
     */
    public void setCurrentUser(Student student) {
        this.currentUser = student;
    }

    /**
     * Simple email format validation helper.
     */
    private boolean isValidEmail(String email) {
        return email != null && email.contains("@") && email.contains(".");
    }
}
