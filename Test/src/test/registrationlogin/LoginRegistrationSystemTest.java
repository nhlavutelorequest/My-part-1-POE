/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
package registrationlogin;

import org.junit.Before;
import org.junit.Test;
import org.junit.After;
import static org.junit.Assert.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.SQLException;

/**
 *
 * @author RC_Student_lab
 */
public class LoginRegistrationSystemTest {
    
    private LoginRegistrationSystem loginSystem;
    private Connection testConnection;
    private static final String TEST_DB_URL = "jdbc:mysql://localhost:3306/";
    private static final String TEST_DB_NAME = "test_userdb";
    private static final String TEST_USER = "root";
    private static final String TEST_PASSWORD = "Request10?";
    
    public LoginRegistrationSystemTest() {
    }
    
    @Before
    public void setUp() {
        try {
            // Set up test database
            Class.forName("com.mysql.cj.jdbc.Driver");
            testConnection = DriverManager.getConnection(TEST_DB_URL, TEST_USER, TEST_PASSWORD);
            
            // Create test database
            Statement stmt = testConnection.createStatement();
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + TEST_DB_NAME);
            testConnection.close();
            
            // Connect to test database
            testConnection = DriverManager.getConnection(TEST_DB_URL + TEST_DB_NAME, TEST_USER, TEST_PASSWORD);
            
            // Create test users table
            String createTable = "CREATE TABLE IF NOT EXISTS users (" +
                                "id INT AUTO_INCREMENT PRIMARY KEY," +
                                "name VARCHAR(100)," +
                                "surname VARCHAR(100)," +
                                "phone VARCHAR(20)," +
                                "username VARCHAR(100) UNIQUE," +
                                "password VARCHAR(255))";
            stmt = testConnection.createStatement();
            stmt.execute(createTable);
            
            // Clear any existing data
            stmt.executeUpdate("DELETE FROM users");
            
            // Insert test user
            String insertUser = "INSERT INTO users (name, surname, phone, username, password) " +
                               "VALUES ('Test', 'User', '+27123456789', 'testuser', 'password123')";
            stmt.executeUpdate(insertUser);
            
            // Initialize the system under test
            loginSystem = new LoginRegistrationSystem() {
                // Override the connection method to use our test database
                protected void connectToDatabase() {
                    try {
                        Class.forName("com.mysql.cj.jdbc.Driver");
                        connection = DriverManager.getConnection(TEST_DB_URL + TEST_DB_NAME, TEST_USER, TEST_PASSWORD);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                
                // Override GUI creation to avoid showing UI during tests
                protected void createAndShowGUI() {
                    // Do nothing - we don't want UI in tests
                }
            };
            
        } catch (Exception e) {
            e.printStackTrace();
            fail("Test setup failed: " + e.getMessage());
        }
    }
    
    @After
    public void tearDown() {
        try {
            if (testConnection != null && !testConnection.isClosed()) {
                // Clean up - drop test database
                Statement stmt = testConnection.createStatement();
                stmt.executeUpdate("DROP DATABASE IF EXISTS " + TEST_DB_NAME);
                testConnection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void testValidateLogin() {
        // Test valid login
        assertTrue("Valid login should return true", 
                  loginSystem.validateLogin("testuser", "password123"));
        
        // Test invalid username
        assertFalse("Invalid username should return false", 
                   loginSystem.validateLogin("wronguser", "password123"));
        
        // Test invalid password
        assertFalse("Invalid password should return false", 
                   loginSystem.validateLogin("testuser", "wrongpassword"));
        
        // Test empty credentials
        assertFalse("Empty credentials should return false", 
                   loginSystem.validateLogin("", ""));
    }
    
    @Test
    public void testRegisterUser() {
        // Test registering a new user
        boolean result = loginSystem.registerUser("John", "Doe", "+27987654321", 
                                                "johndoe", "pass123");
        assertTrue("Registration of new user should succeed", result);
        
        // Verify the user can log in
        assertTrue("New user should be able to log in", 
                  loginSystem.validateLogin("johndoe", "pass123"));
        
        // Test duplicate username (should fail)
        result = loginSystem.registerUser("Jane", "Doe", "+27987654322", 
                                         "johndoe", "different");
        assertFalse("Registration with duplicate username should fail", result);
    }
    
    @Test
    public void testValidateRegistration() {
        // Test valid registration details
        assertTrue("Valid registration details should be accepted",
                  loginSystem.validateRegistration("password", "password", "+27123456789"));
        
        // Test password mismatch
        assertFalse("Password mismatch should be rejected",
                   loginSystem.validateRegistration("password1", "password2", "+27123456789"));
        
        // Test empty passwords
        assertFalse("Empty passwords should be rejected",
                   loginSystem.validateRegistration("", "", "+27123456789"));
        
        // Test invalid phone number format
        assertFalse("Invalid phone number format should be rejected",
                   loginSystem.validateRegistration("password", "password", "0123456789"));
        
        assertFalse("Invalid phone number format should be rejected",
                   loginSystem.validateRegistration("password", "password", "+271234"));
    }
    
    @Test
    public void testMain() {
        // This is a simple test to ensure the main method doesn't throw exceptions
        try {
            
            String[] args = new String[0];
            LoginRegistrationSystem.main(args);
            // If we get here without exception, the test passes
            assertTrue(true);
        } catch (Exception e) {
            fail("Main method threw exception: " + e.getMessage());
        }
    }
}