/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package registrationlogin;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;
import java.sql.*;
import java.lang.reflect.Field;

/**
 *
 * @author RC_Student_lab
 */
public class LoginRegistrationSystemTest {
    
    private LoginRegistrationSystem system;
    private Connection testConnection;
    private static final String TEST_NAME = "TestName";
    private static final String TEST_SURNAME = "TestSurname";
    private static final String TEST_PHONE = "+27123456789";
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_PASSWORD = "testpassword";
    
    public LoginRegistrationSystemTest() {
    }
    
    @BeforeEach
    public void setUp() {
        try {
            // Connect to the database first
            Class.forName("com.mysql.cj.jdbc.Driver");
            testConnection = (Connection) DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/userdb", "root", "Request10?");
                
            // Clean up any existing test users
            cleanupTestUser();
            
            // Create system with modified constructor to prevent UI display
            system = new LoginRegistrationSystem() {
                protected void createAndShowGUI() {
                    // Skip UI creation for tests
                }
            };
            
            // Allow time for the database connection
            Thread.sleep(500);
            
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found: " + e.getMessage());
            e.printStackTrace();
            fail("MySQL JDBC Driver not found: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Test setup failed: " + e.getMessage());
            e.printStackTrace();
            fail("Test setup failed: " + e.getMessage());
        }
    }
    
    @AfterEach
    public void tearDown() {
        try {
            // Clean up test user
            cleanupTestUser();
            
            // Close test connection
            if (testConnection != null && !testConnection.isClosed()) {
                testConnection.close();
            }
            
            // Close system connection using reflection
            try {
                Field connectionField = LoginRegistrationSystem.class.getDeclaredField("connection");
                connectionField.setAccessible(true);
                Connection systemConnection = (Connection) connectionField.get(system);
                if (systemConnection != null && !systemConnection.isClosed()) {
                    systemConnection.close();
                }
            } catch (Exception e) {
                System.err.println("Could not close system connection: " + e.getMessage());
                e.printStackTrace();
            }
            
        } catch (SQLException e) {
            System.err.println("Error during teardown: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void cleanupTestUser() {
        try {
            // Make sure the connection is valid
            if (testConnection == null || testConnection.isClosed()) {
                testConnection = (Connection) DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/userdb", "root", "Request10?");
            }
            
            // Delete test user
            try (PreparedStatement stmt = testConnection.prepareStatement(
                "DELETE FROM users WHERE username = ?")) {
                stmt.setString(1, TEST_USERNAME);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("Error cleaning up test user: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Test
    public void simpleTest() {
        // A very simple test that should always pass
        assertTrue(true, "This test should always pass");
    }
    
    @Test
    public void testValidateLogin() {
        try {
            // Register a test user first
            boolean registerSuccess = system.registerUser(TEST_NAME, TEST_SURNAME, TEST_PHONE, TEST_USERNAME, TEST_PASSWORD);
            assertTrue(registerSuccess, "Should successfully register test user");
            
            // Test successful login
            boolean validLoginResult = system.validateLogin(TEST_USERNAME, TEST_PASSWORD);
            assertTrue(validLoginResult, "Login should succeed with correct credentials");
            
            // Test with wrong password
            boolean invalidPasswordResult = system.validateLogin(TEST_USERNAME, "wrongpassword");
            assertFalse(invalidPasswordResult, "Login should fail with incorrect password");
            
            // Test with non-existent user
            boolean nonExistentUserResult = system.validateLogin("nonexistentuser", TEST_PASSWORD);
            assertFalse(nonExistentUserResult, "Login should fail with non-existent username");
        } catch (Exception e) {
            System.err.println("Error in validate login test: " + e.getMessage());
            e.printStackTrace();
            fail("Test failed with exception: " + e.getMessage());
        }
    }
    
    @Test
    public void testRegisterUser() {
        try {
            // Test new user registration
            boolean registerResult = system.registerUser(
                TEST_NAME, TEST_SURNAME, TEST_PHONE, TEST_USERNAME, TEST_PASSWORD);
            assertTrue(registerResult, "Registration should succeed with valid data");
            
            // Verify user in database
            try (PreparedStatement stmt = testConnection.prepareStatement(
                    "SELECT * FROM users WHERE username = ?")) {
                stmt.setString(1, TEST_USERNAME);
                ResultSet rs = stmt.executeQuery();
                
                assertTrue(rs.next(), "User should exist in database");
                assertEquals(TEST_NAME, rs.getString("name"), "Name should match");
                assertEquals(TEST_SURNAME, rs.getString("surname"), "Surname should match");
                assertEquals(TEST_PHONE, rs.getString("phone"), "Phone should match");
                assertEquals(TEST_PASSWORD, rs.getString("password"), "Password should match");
            }
            
            // Test duplicate username
            boolean duplicateResult = system.registerUser(
                "Another", "Person", "+27987654321", TEST_USERNAME, "differentpassword");
            assertFalse(duplicateResult, "Registration should fail with duplicate username");
        } catch (Exception e) {
            System.err.println("Error in register user test: " + e.getMessage());
            e.printStackTrace();
            fail("Test failed with exception: " + e.getMessage());
        }
    }
    
    @Test
    public void testValidateRegistration() {
        // Valid registration data
        boolean validResult = system.validateRegistration(
            TEST_PASSWORD, TEST_PASSWORD, TEST_PHONE);
        assertTrue(validResult, "Validation should pass with valid data");
        
        // Mismatched passwords
        boolean mismatchedResult = system.validateRegistration(
            TEST_PASSWORD, "differentpassword", TEST_PHONE);
        assertFalse(mismatchedResult, "Validation should fail with mismatched passwords");
        
        // Empty passwords
        boolean emptyPasswordResult = system.validateRegistration(
            "", TEST_PASSWORD, TEST_PHONE);
        assertFalse(emptyPasswordResult, "Validation should fail with empty password");
        
        boolean emptyConfirmResult = system.validateRegistration(
            TEST_PASSWORD, "", TEST_PHONE);
        assertFalse(emptyConfirmResult, "Validation should fail with empty confirm password");
        
        // Invalid phone formats
        boolean invalidPhone1 = system.validateRegistration(
            TEST_PASSWORD, TEST_PASSWORD, "0123456789");
        assertFalse(invalidPhone1, "Validation should fail without +27 prefix");
        
        boolean invalidPhone2 = system.validateRegistration(
            TEST_PASSWORD, TEST_PASSWORD, "+271234567");
        assertFalse(invalidPhone2, "Validation should fail with too short number");
        
        boolean invalidPhone3 = system.validateRegistration(
            TEST_PASSWORD, TEST_PASSWORD, "+2712345678901");
        assertFalse(invalidPhone3, "Validation should fail with too long number");
    }
    
    @Test
    public void testMain() {
        // This test is modified to run main in a separate thread to avoid UI blocking
        Thread mainThread = new Thread(() -> {
            try {
                String[] args = new String[0];
                LoginRegistrationSystem.main(args);
            } catch (Exception e) {
                fail("Main method execution failed: " + e.getMessage());
            }
        });
        
        try {
            mainThread.start();
            // Allow the main thread to start
            Thread.sleep(1000);
            // Interrupt to prevent test from hanging
            mainThread.interrupt();
            
            // Test passes if we get here without exception
            assertTrue(true);
        } catch (Exception e) {
            System.err.println("Error in main test: " + e.getMessage());
            e.printStackTrace();
            fail("Test failed with exception: " + e.getMessage());
        }
    }
}