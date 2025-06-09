package Library_management;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Vector;

public class LibrarianSystem extends JFrame {
    private Connection conn;
    private JTabbedPane tabbedPane;
    
    // Student Registration Components
    private JTextField studentIdField, studentNameField, emailField, phoneField;
    private JTextArea addressArea;
    private JPasswordField passwordField;
    
    // Book Management Components
    private JTextField bookCodeField, bookNameField, authorField, publisherField;
    private JTextField yearField, isbnField, totalCopiesField;
    private JTable booksTable;
    private DefaultTableModel booksTableModel;
    
    // Fine Management Components
    private JTable finesTable;
    private DefaultTableModel finesTableModel;
    
    public LibrarianSystem() {
        initializeDatabase();
        initializeGUI();
        loadBooksData();
        loadFinesData();
    }
    
    private void initializeDatabase() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/library_management", 
                "root", "Squard@502"
            );
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Database connection failed: " + e.getMessage());
            System.exit(1);
        }
    }
    
    private void initializeGUI() {
        setTitle("Library Management System - Librarian Panel");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);
        
        tabbedPane = new JTabbedPane();
        
        tabbedPane.addTab("Register Student", createStudentPanel());
        tabbedPane.addTab("Manage Books", createBooksPanel());
        tabbedPane.addTab("View Fines", createFinesPanel());
        
        add(tabbedPane);
    }
    
    private JPanel createStudentPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Student ID
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Student ID:"), gbc);
        gbc.gridx = 1;
        studentIdField = new JTextField(20);
        panel.add(studentIdField, gbc);
        
        // Password
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        passwordField = new JPasswordField(20);
        panel.add(passwordField, gbc);
        
        // Student Name
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        studentNameField = new JTextField(20);
        panel.add(studentNameField, gbc);
        
        // Email
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        emailField = new JTextField(20);
        panel.add(emailField, gbc);
        
        // Phone
        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("Phone:"), gbc);
        gbc.gridx = 1;
        phoneField = new JTextField(20);
        panel.add(phoneField, gbc);
        
        // Address
        gbc.gridx = 0; gbc.gridy = 5;
        panel.add(new JLabel("Address:"), gbc);
        gbc.gridx = 1;
        addressArea = new JTextArea(4, 20);
        addressArea.setLineWrap(true);
        panel.add(new JScrollPane(addressArea), gbc);
        
        // Register Button
        gbc.gridx = 1; gbc.gridy = 6;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JButton registerBtn = new JButton("Register Student");
        registerBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                registerStudent();
            }
        });
        panel.add(registerBtn, gbc);
        
        return panel;
    }
    
    private JPanel createBooksPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Row 1: Book Code and Book Name
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Book Code:"), gbc);
        gbc.gridx = 1;
        bookCodeField = new JTextField(12);
        formPanel.add(bookCodeField, gbc);
        
        gbc.gridx = 2;
        formPanel.add(new JLabel("Book Name:"), gbc);
        gbc.gridx = 3;
        bookNameField = new JTextField(20);
        formPanel.add(bookNameField, gbc);
        
        // Row 2: Author and Publisher
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Author:"), gbc);
        gbc.gridx = 1;
        authorField = new JTextField(12);
        formPanel.add(authorField, gbc);
        
        gbc.gridx = 2;
        formPanel.add(new JLabel("Publisher:"), gbc);
        gbc.gridx = 3;
        publisherField = new JTextField(20);
        formPanel.add(publisherField, gbc);
        
        // Row 3: Year and ISBN
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Year:"), gbc);
        gbc.gridx = 1;
        yearField = new JTextField(12);
        formPanel.add(yearField, gbc);
        
        gbc.gridx = 2;
        formPanel.add(new JLabel("ISBN:"), gbc);
        gbc.gridx = 3;
        isbnField = new JTextField(20);
        formPanel.add(isbnField, gbc);
        
        // Row 4: Total Copies
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Total Copies:"), gbc);
        gbc.gridx = 1;
        totalCopiesField = new JTextField(12);
        formPanel.add(totalCopiesField, gbc);
        
        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton addBtn = new JButton("Add Book");
        JButton updateBtn = new JButton("Update Book");
        JButton deleteBtn = new JButton("Delete Book");
        JButton clearBtn = new JButton("Clear Fields");
        
        addBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addBook();
            }
        });
        
        updateBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateBook();
            }
        });
        
        deleteBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                deleteBook();
            }
        });
        
        clearBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clearBookFields();
            }
        });
        
        buttonPanel.add(addBtn);
        buttonPanel.add(updateBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(clearBtn);
        
        // Table
        String[] columns = {"Book Code", "Book Name", "Author", "Publisher", "Year", "ISBN", "Total Copies", "Available"};
        booksTableModel = new DefaultTableModel(columns, 0);
        booksTable = new JTable(booksTableModel);
        booksTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        booksTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                fillBookFields();
            }
        });
        
        JScrollPane tableScrollPane = new JScrollPane(booksTable);
        tableScrollPane.setPreferredSize(new Dimension(800, 300));
        
        mainPanel.add(formPanel, BorderLayout.NORTH);
        mainPanel.add(buttonPanel, BorderLayout.CENTER);
        mainPanel.add(tableScrollPane, BorderLayout.SOUTH);
        
        return mainPanel;
    }
    
    private JPanel createFinesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Header Panel
        JPanel headerPanel = new JPanel(new FlowLayout());
        JButton refreshBtn = new JButton("Refresh Fines Data");
        refreshBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loadFinesData();
            }
        });
        headerPanel.add(refreshBtn);
        
        // Table
        String[] columns = {"Issue ID", "Student ID", "Student Name", "Book Code", "Book Name", "Due Date", "Fine Amount"};
        finesTableModel = new DefaultTableModel(columns, 0);
        finesTable = new JTable(finesTableModel);
        finesTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        
        JScrollPane tableScrollPane = new JScrollPane(finesTable);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(tableScrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void registerStudent() {
        String studentId = studentIdField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        String name = studentNameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String address = addressArea.getText().trim();
        
        if (studentId.isEmpty() || password.isEmpty() || name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill required fields: Student ID, Password, Name");
            return;
        }
        
        try {
            // Create students_auth table if not exists
            String createAuthTable = "CREATE TABLE IF NOT EXISTS students_auth (" +
                "student_id VARCHAR(20) PRIMARY KEY, " +
                "password VARCHAR(255) NOT NULL, " +
                "FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE)";
            conn.createStatement().execute(createAuthTable);
            
            // Insert student
            String insertStudent = "INSERT INTO students (student_id, student_name, email, phone, address) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(insertStudent);
            pstmt.setString(1, studentId);
            pstmt.setString(2, name);
            pstmt.setString(3, email.isEmpty() ? null : email);
            pstmt.setString(4, phone.isEmpty() ? null : phone);
            pstmt.setString(5, address.isEmpty() ? null : address);
            pstmt.executeUpdate();
            
            // Insert password
            String insertAuth = "INSERT INTO students_auth (student_id, password) VALUES (?, ?)";
            PreparedStatement pstmt2 = conn.prepareStatement(insertAuth);
            pstmt2.setString(1, studentId);
            pstmt2.setString(2, password);
            pstmt2.executeUpdate();
            
            JOptionPane.showMessageDialog(this, "Student registered successfully!");
            clearStudentFields();
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error registering student: " + e.getMessage());
        }
    }
    
    private void clearStudentFields() {
        studentIdField.setText("");
        passwordField.setText("");
        studentNameField.setText("");
        emailField.setText("");
        phoneField.setText("");
        addressArea.setText("");
    }
    
    private void addBook() {
        String bookCode = bookCodeField.getText().trim();
        String bookName = bookNameField.getText().trim();
        String author = authorField.getText().trim();
        String publisher = publisherField.getText().trim();
        String yearStr = yearField.getText().trim();
        String isbn = isbnField.getText().trim();
        String copiesStr = totalCopiesField.getText().trim();
        
        if (bookCode.isEmpty() || bookName.isEmpty() || author.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill required fields: Book Code, Name, Author");
            return;
        }
        
        try {
            int year = yearStr.isEmpty() ? 0 : Integer.parseInt(yearStr);
            int totalCopies = copiesStr.isEmpty() ? 1 : Integer.parseInt(copiesStr);
            
            String sql = "INSERT INTO books (book_code, book_name, author, publisher, year_published, isbn, total_copies, available_copies) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, bookCode);
            pstmt.setString(2, bookName);
            pstmt.setString(3, author);
            pstmt.setString(4, publisher.isEmpty() ? null : publisher);
            pstmt.setInt(5, year);
            pstmt.setString(6, isbn.isEmpty() ? null : isbn);
            pstmt.setInt(7, totalCopies);
            pstmt.setInt(8, totalCopies);
            
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Book added successfully!");
            clearBookFields();
            loadBooksData();
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers for Year and Total Copies");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error adding book: " + e.getMessage());
        }
    }
    
    private void updateBook() {
        String bookCode = bookCodeField.getText().trim();
        if (bookCode.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a book to update");
            return;
        }
        
        try {
            String sql = "UPDATE books SET book_name=?, author=?, publisher=?, year_published=?, isbn=?, total_copies=? WHERE book_code=?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, bookNameField.getText().trim());
            pstmt.setString(2, authorField.getText().trim());
            pstmt.setString(3, publisherField.getText().trim());
            pstmt.setInt(4, Integer.parseInt(yearField.getText().trim()));
            pstmt.setString(5, isbnField.getText().trim());
            pstmt.setInt(6, Integer.parseInt(totalCopiesField.getText().trim()));
            pstmt.setString(7, bookCode);
            
            int rowsUpdated = pstmt.executeUpdate();
            if (rowsUpdated > 0) {
                JOptionPane.showMessageDialog(this, "Book updated successfully!");
                loadBooksData();
            } else {
                JOptionPane.showMessageDialog(this, "Book not found");
            }
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers for Year and Total Copies");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error updating book: " + e.getMessage());
        }
    }
    
    private void deleteBook() {
        String bookCode = bookCodeField.getText().trim();
        if (bookCode.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a book to delete");
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete this book?", 
            "Confirm Delete", 
            JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                String sql = "DELETE FROM books WHERE book_code=?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, bookCode);
                
                int rowsDeleted = pstmt.executeUpdate();
                if (rowsDeleted > 0) {
                    JOptionPane.showMessageDialog(this, "Book deleted successfully!");
                    clearBookFields();
                    loadBooksData();
                } else {
                    JOptionPane.showMessageDialog(this, "Book not found");
                }
                
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error deleting book: " + e.getMessage());
            }
        }
    }
    
    private void clearBookFields() {
        bookCodeField.setText("");
        bookNameField.setText("");
        authorField.setText("");
        publisherField.setText("");
        yearField.setText("");
        isbnField.setText("");
        totalCopiesField.setText("");
    }
    
    private void fillBookFields() {
        int selectedRow = booksTable.getSelectedRow();
        if (selectedRow >= 0) {
            bookCodeField.setText(booksTable.getValueAt(selectedRow, 0).toString());
            bookNameField.setText(booksTable.getValueAt(selectedRow, 1).toString());
            authorField.setText(booksTable.getValueAt(selectedRow, 2).toString());
            publisherField.setText(booksTable.getValueAt(selectedRow, 3) != null ? 
                booksTable.getValueAt(selectedRow, 3).toString() : "");
            yearField.setText(booksTable.getValueAt(selectedRow, 4).toString());
            isbnField.setText(booksTable.getValueAt(selectedRow, 5) != null ? 
                booksTable.getValueAt(selectedRow, 5).toString() : "");
            totalCopiesField.setText(booksTable.getValueAt(selectedRow, 6).toString());
        }
    }
    
    private void loadBooksData() {
        try {
            booksTableModel.setRowCount(0);
            String sql = "SELECT * FROM books ORDER BY book_code";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                Vector<Object> row = new Vector<Object>();
                row.add(rs.getString("book_code"));
                row.add(rs.getString("book_name"));
                row.add(rs.getString("author"));
                row.add(rs.getString("publisher"));
                row.add(rs.getInt("year_published"));
                row.add(rs.getString("isbn"));
                row.add(rs.getInt("total_copies"));
                row.add(rs.getInt("available_copies"));
                booksTableModel.addRow(row);
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading books: " + e.getMessage());
        }
    }
    
    private void loadFinesData() {
        try {
            finesTableModel.setRowCount(0);
            String sql = "SELECT ib.issue_id, ib.student_id, s.student_name, ib.book_code, b.book_name, ib.due_date, " +
                        "CASE WHEN ib.due_date < CURDATE() AND ib.status = 'ACTIVE' THEN " +
                        "DATEDIFF(CURDATE(), ib.due_date) * 5.00 ELSE ib.fine_amount END as fine_amount " +
                        "FROM issued_books ib " +
                        "JOIN students s ON ib.student_id = s.student_id " +
                        "JOIN books b ON ib.book_code = b.book_code " +
                        "WHERE (ib.fine_amount > 0 OR (ib.due_date < CURDATE() AND ib.status = 'ACTIVE')) " +
                        "ORDER BY ib.due_date";
            
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                Vector<Object> row = new Vector<Object>();
                row.add(rs.getInt("issue_id"));
                row.add(rs.getString("student_id"));
                row.add(rs.getString("student_name"));
                row.add(rs.getString("book_code"));
                row.add(rs.getString("book_name"));
                row.add(rs.getDate("due_date"));
                row.add(String.format("%.2f", rs.getDouble("fine_amount")));
                finesTableModel.addRow(row);
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading fines: " + e.getMessage());
        }
    }
    
    public static void runLibrarianSystem() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                // Create and show the GUI
                LibrarianSystem frame = new LibrarianSystem();
                frame.setVisible(true);
            }
        });
    }
}