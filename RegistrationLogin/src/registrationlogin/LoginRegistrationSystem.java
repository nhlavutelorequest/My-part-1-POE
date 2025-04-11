/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package registrationlogin;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class LoginRegistrationSystem {
    private JFrame frame;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField nameField;
    private JTextField surnameField;
    private JTextField phoneNumberField;
    private JPasswordField confirmPasswordField;

    Connection connection;

    public LoginRegistrationSystem() {
        connectToDatabase();
        createUsersTable();
        createAndShowGUI();
    }

    // Connecting to MySQL database
    private void connectToDatabase() {
        try {
            // Explicitly load the MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Connect to MySQL server first
            String url = "jdbc:mysql://localhost:3306/";
            String username = "root";
            String password = "Request10?";
            
            // Firstly establishing a connection to MySQL
            connection = DriverManager.getConnection(url, username, password);
            
            // Database
            Statement stmt = connection.createStatement();
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS userdb");
            connection.close();
            
            // database
            connection = DriverManager.getConnection(url + "userdb", username, password);
            System.out.println("Connected to the database successfully.");
            
        } catch (ClassNotFoundException e) {
            System.err.println("Error: MySQL JDBC Driver not found!");
            JOptionPane.showMessageDialog(null, 
                "Database driver not found. Please add the MySQL connector JAR to your project.",
                "Driver Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            System.exit(1);
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
            JOptionPane.showMessageDialog(null, 
                "Failed to connect to database: " + e.getMessage(),
                "Connection Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            System.exit(1);
        }
    }

    //  users table 
    private void createUsersTable() {
        String sql = "CREATE TABLE IF NOT EXISTS users (" +
                     "id INT AUTO_INCREMENT PRIMARY KEY," +
                     "name VARCHAR(100)," +
                     "surname VARCHAR(100)," +
                     "phone VARCHAR(20)," +
                     "username VARCHAR(100) UNIQUE," +
                     "password VARCHAR(255))"; // Using a larger size for password
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            System.out.println("Users table created or already exists.");
        } catch (SQLException e) {
            System.err.println("Error creating users table: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error creating database table: " + e.getMessage());
        }
    }

    // Validate login using database
    public boolean validateLogin(String username, String password) {
        String query = "SELECT * FROM users WHERE username=? AND password=?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.err.println("Login validation error: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Database error during login: " + e.getMessage());
            return false;
        }
    }

    // Registering user in database
    public boolean registerUser(String name, String surname, String phone, String username, String password) {
        String sql = "INSERT INTO users(name, surname, phone, username, password) VALUES(?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setString(2, surname);
            stmt.setString(3, phone);
            stmt.setString(4, username);
            stmt.setString(5, password);
            stmt.executeUpdate();
            System.out.println("User registered successfully: " + username);
            return true;
        } catch (SQLException e) {
            System.err.println("Registration error: " + e.getMessage());
            e.printStackTrace();
            if (e.getMessage().contains("Duplicate")) {
                JOptionPane.showMessageDialog(frame, "Error: Username already exists.");
            } else {
                JOptionPane.showMessageDialog(frame, "Registration error: " + e.getMessage());
            }
            return false;
        }
    }

    public boolean validateRegistration(String password, String confirmPassword, String phoneNumber) {
        if (password.isEmpty() || confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Password fields cannot be empty.");
            return false;
        }
        
        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(frame, "Passwords do not match.");
            return false;
        }
        
        if (!phoneNumber.matches("^\\+27\\d{9}$")) {
            JOptionPane.showMessageDialog(frame, "Phone number must be in format: +27XXXXXXXXX");
            return false;
        }
        
        return true;
    }

    private void createAndShowGUI() {
        frame = new JFrame("Login/Registration System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 500);  // Made taller to fit all fields better
        frame.setLayout(new CardLayout());

        // Login panel
        JPanel loginPanel = new JPanel();
        loginPanel.setLayout(new BoxLayout(loginPanel, BoxLayout.Y_AXIS));
        loginPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel usernameLabel = new JLabel("Username:");
        usernameField = new JTextField(15);

        JLabel passwordLabel = new JLabel("Password:");
        passwordField = new JPasswordField(15);

        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());
                
                if (username.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Username and password cannot be empty");
                    return;
                }
                
                if (validateLogin(username, password)) {
                    JOptionPane.showMessageDialog(frame, "Login successful!");
                } else {
                    JOptionPane.showMessageDialog(frame, "Invalid username or password");
                }
            }
        });

        
        loginPanel.add(Box.createVerticalStrut(10));
        loginPanel.add(usernameLabel);
        loginPanel.add(usernameField);
        loginPanel.add(Box.createVerticalStrut(10));
        loginPanel.add(passwordLabel);
        loginPanel.add(passwordField);
        loginPanel.add(Box.createVerticalStrut(20));
        loginPanel.add(loginButton);

        // Registration panel
        JPanel registrationPanel = new JPanel();
        registrationPanel.setLayout(new BoxLayout(registrationPanel, BoxLayout.Y_AXIS));
        registrationPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel nameLabel = new JLabel("Name:");
        nameField = new JTextField(15);

        JLabel surnameLabel = new JLabel("Surname:");
        surnameField = new JTextField(15);

        JLabel phoneNumberLabel = new JLabel("Phone Number (Format: +27XXXXXXXXX):");
        phoneNumberField = new JTextField(15);

        JLabel usernameRegistrationLabel = new JLabel("Username:");
        JTextField usernameRegistrationField = new JTextField(15);

        JLabel passwordRegistrationLabel = new JLabel("Password:");
        JPasswordField passwordRegistrationField = new JPasswordField(15);

        JLabel confirmPasswordLabel = new JLabel("Confirm Password:");
        confirmPasswordField = new JPasswordField(15);

        JButton registerButton = new JButton("Register");
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = nameField.getText();
                String surname = surnameField.getText();
                String phoneNumber = phoneNumberField.getText();
                String username = usernameRegistrationField.getText();
                String password = new String(passwordRegistrationField.getPassword());
                String confirmPassword = new String(confirmPasswordField.getPassword());

                // Basic validation
                if (name.isEmpty() || surname.isEmpty() || username.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "All fields are required.");
                    return;
                }

                if (validateRegistration(password, confirmPassword, phoneNumber)) {
                    boolean success = registerUser(name, surname, phoneNumber, username, password);
                    if (success) {
                        JOptionPane.showMessageDialog(frame, "Registration successful!");
                        // Clear fields after successful registration
                        nameField.setText("");
                        surnameField.setText("");
                        phoneNumberField.setText("");
                        usernameRegistrationField.setText("");
                        passwordRegistrationField.setText("");
                        confirmPasswordField.setText("");
                    }
                }
            }
        });

        registrationPanel.add(Box.createVerticalStrut(5));
        registrationPanel.add(nameLabel);
        registrationPanel.add(nameField);
        registrationPanel.add(Box.createVerticalStrut(5));
        registrationPanel.add(surnameLabel);
        registrationPanel.add(surnameField);
        registrationPanel.add(Box.createVerticalStrut(5));
        registrationPanel.add(phoneNumberLabel);
        registrationPanel.add(phoneNumberField);
        registrationPanel.add(Box.createVerticalStrut(5));
        registrationPanel.add(usernameRegistrationLabel);
        registrationPanel.add(usernameRegistrationField);
        registrationPanel.add(Box.createVerticalStrut(5));
        registrationPanel.add(passwordRegistrationLabel);
        registrationPanel.add(passwordRegistrationField);
        registrationPanel.add(Box.createVerticalStrut(5));
        registrationPanel.add(confirmPasswordLabel);
        registrationPanel.add(confirmPasswordField);
        registrationPanel.add(Box.createVerticalStrut(15));
        registrationPanel.add(registerButton);

        // Switch panels
        CardLayout cardLayout = (CardLayout) frame.getContentPane().getLayout();

        JButton switchToRegister = new JButton("Need an account? Register");
        switchToRegister.addActionListener(e -> cardLayout.show(frame.getContentPane(), "registration"));
        
        JButton switchToLogin = new JButton("Already have an account? Login");
        switchToLogin.addActionListener(e -> cardLayout.show(frame.getContentPane(), "login"));

        loginPanel.add(Box.createVerticalStrut(20));
        loginPanel.add(switchToRegister);
        
        registrationPanel.add(Box.createVerticalStrut(20));
        registrationPanel.add(switchToLogin);

        frame.add(loginPanel, "login");
        frame.add(registrationPanel, "registration");
        
        // Center the frame on the screen
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginRegistrationSystem::new);
    }
}
