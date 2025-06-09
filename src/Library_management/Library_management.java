package Library_management;
import Library_management.StudentSection;
import Library_management.LibrarianSystem;
import javax.swing.*;
import java.io.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class Library_management {
    private static Connection conn;
    private static int currentUserId;
    private static String currentUserName;
    private static String userType;

    public static void main(String[] args) {
        // Initialize database connection
        initializeDatabase();
        
        // Create login window
        createLoginWindow();
    }

    private static void initializeDatabase() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/library_management", 
                "root", "Squard@502"
            );
            System.out.println("Database connected successfully");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Database connection failed: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void createLoginWindow() {
        // Frame Part
        JFrame frame = new JFrame("Liberary Management System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(null);
        frame.setSize(500, 400);
        frame.getContentPane().setBackground(new Color(0x212121));
        frame.setResizable(false);

        // The image Label
        ImageIcon usr_img = new ImageIcon("user.png");
        Image img = usr_img.getImage();
        Image resized_img = img.getScaledInstance(100, 100, Image.SCALE_SMOOTH);
        ImageIcon resized_usr_img = new ImageIcon(resized_img);
        JLabel usr_img_label = new JLabel(resized_usr_img);
        usr_img_label.setBounds(200, 25, 100, 100);
        frame.add(usr_img_label);

        // The Login Form
        JLabel username_label = new JLabel("Enter Username:");
        username_label.setFont(new Font("arial", Font.PLAIN, 18));
        username_label.setBounds(50, 130, 150, 50);
        username_label.setForeground(new Color(0x1DB954));
        frame.add(username_label);

        JTextArea username_field = new JTextArea();
        username_field.setFont(new Font("arial", Font.PLAIN, 18));
        username_field.setBounds(200, 142, 200, 22);
        frame.add(username_field);

        JLabel password_label = new JLabel("Enter Password:");
        password_label.setFont(new Font("arial", Font.PLAIN, 18));
        password_label.setBounds(50, 170, 150, 50);
        password_label.setForeground(new Color(0x1DB954));
        frame.add(password_label);

        JTextArea password_field = new JTextArea();
        password_field.setFont(new Font("arial", Font.PLAIN, 18));
        password_field.setBounds(200, 180, 200, 22);
        frame.add(password_field);

        JButton student_login_button = new JButton("Student Login");
        student_login_button.setBackground(new Color(0x1DB954));
        student_login_button.setBounds(50, 220, 150, 50);
        student_login_button.setForeground(Color.BLACK);
        frame.add(student_login_button);

        JButton librarian_login_button = new JButton("Librarian Login");
        librarian_login_button.setBackground(new Color(0x1DB954));
        librarian_login_button.setBounds(250, 220, 150, 50);
        frame.add(librarian_login_button);

        // Student Login Action
        student_login_button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = username_field.getText().trim();
                String password = password_field.getText().trim();
                
                if (username.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Please enter both username and password!");
                    return;
                }
                
                if (authenticateStudent(username, password)) {
                    frame.dispose();
                    // Call your student portal function here
                    openStudentPortal();
                } else {
                    JOptionPane.showMessageDialog(frame, "Invalid student credentials!");
                    password_field.setText("");
                }
            }
        });

        // Librarian Login Action
        librarian_login_button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = username_field.getText().trim();
                String password = password_field.getText().trim();
                
                if (username.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Please enter both username and password!");
                    return;
                }
                
                if (authenticateLibrarian(username, password)) {
                    frame.dispose();
                    // Call your librarian portal function here
                    openLibrarianPortal();
                } else {
                    JOptionPane.showMessageDialog(frame, "Invalid librarian credentials!");
                    password_field.setText("");
                }
            }
        });

        frame.setVisible(true);
    }

    private static boolean authenticateStudent(String studentId, String password) {
    try {
        String query = "SELECT s.* FROM students_auth sa " +
                       "JOIN students s ON sa.student_id = s.student_id " +
                       "WHERE sa.student_id = ? AND sa.password = ?";
        PreparedStatement pst = conn.prepareStatement(query);
        pst.setString(1, studentId);
        pst.setString(2, password);
        
        ResultSet rs = pst.executeQuery();
        
        if (rs.next()) {
            currentUserName = rs.getString("student_name");
            userType = "student";
            return true;
        }
        return false;
        
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(null, "Authentication error: " + e.getMessage());
        return false;
    }
}

    private static boolean authenticateLibrarian(String username, String password) {
        try {
            String query = "SELECT * FROM librarians WHERE username = ? AND password = ? AND is_active = TRUE";
            PreparedStatement pst = conn.prepareStatement(query);
            pst.setString(1, username);
            pst.setString(2, password);
            
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                currentUserId = rs.getInt("librarian_id");
                currentUserName = rs.getString("full_name");
                userType = "librarian";
                return true;
            }
            return false;
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Authentication error: " + e.getMessage());
            return false;
        }
    }

    private static void openStudentPortal() {
        StudentSection.runStudentSide(currentUserName);
    }

    private static void openLibrarianPortal() {
        LibrarianSystem.runLibrarianSystem();
    }

    // Getter methods to access user data from other classes
    public static int getCurrentUserId() {
        return currentUserId;
    }

    public static String getCurrentUserName() {
        return currentUserName;
    }

    public static String getUserType() {
        return userType;
    }

    public static Connection getConnection() {
        return conn;
    }
}
