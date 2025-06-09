package Library_management;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class StudentSection {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/library_management";
    private static final String DB_USERNAME = "root";
    private static final String DB_PASSWORD = "Squard@502";
    
    private static Connection connection;
    private static String currentStudentId = "STU001"; // This should be passed from login
    
    public static void runStudentSide(String studentName) {
        currentStudentId=studentName;
        try {
            // Initialize database connection
            initializeDatabase();
            
            SwingUtilities.invokeLater(() -> createAndShowGUI());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Database connection failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void initializeDatabase() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
            System.out.println("Database connected successfully!");
            
            // Test the connection with a simple query
            String testQuery = "SELECT COUNT(*) as book_count FROM books";
            PreparedStatement testStmt = connection.prepareStatement(testQuery);
            ResultSet testRs = testStmt.executeQuery();
            if (testRs.next()) {
                int bookCount = testRs.getInt("book_count");
                System.out.println("Found " + bookCount + " books in database");
                if (bookCount == 0) {
                    System.out.println("Warning: No books found in database. Please run the sample data insertion script.");
                }
            }
            testStmt.close();
            testRs.close();
            
            // Check if current student exists, if not create one
            ensureStudentExists();
            
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found. Please add mysql-connector-java to your classpath.", e);
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
            throw e;
        }
    }
    
    private static void ensureStudentExists() {
        try {
            // Check if student exists
            String checkQuery = "SELECT COUNT(*) FROM students WHERE student_id = ?";
            PreparedStatement checkStmt = connection.prepareStatement(checkQuery);
            checkStmt.setString(1, currentStudentId);
            ResultSet rs = checkStmt.executeQuery();
            
            if (rs.next() && rs.getInt(1) == 0) {
                // Student doesn't exist, create one
                String insertQuery = "INSERT INTO students (student_id, student_name, email, phone, address) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement insertStmt = connection.prepareStatement(insertQuery);
                insertStmt.setString(1, currentStudentId);
                insertStmt.setString(2, "Default Student");
                insertStmt.setString(3, "student@example.com");
                insertStmt.setString(4, "1234567890");
                insertStmt.setString(5, "Default Address");
                insertStmt.executeUpdate();
                System.out.println("Created default student with ID: " + currentStudentId);
                insertStmt.close();
            }
            
            checkStmt.close();
            rs.close();
        } catch (SQLException e) {
            System.err.println("Error ensuring student exists: " + e.getMessage());
        }
    }
    
    private static void createAndShowGUI() {
        JFrame studentFrame = new JFrame("Library Management System - Student Portal");
        studentFrame.setSize(900, 600);
        studentFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        studentFrame.setLayout(null);
        studentFrame.setResizable(false);
        studentFrame.setLocationRelativeTo(null);

        // The Tabbed Pane
        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(new Color(0x212121));
        tabs.setBounds(0, 0, 900, 600);
        tabs.setFont(new Font("Arial", Font.PLAIN, 14));
        tabs.setForeground(Color.WHITE);

        // Create all panels
        JPanel issuePanel = createIssuePanel();
        JPanel searchPanel = createSearchPanel();
        JPanel myBooksPanel = createMyBooksPanel();
        JPanel finePanel = createFinePanel();
        JPanel requestPanel = createRequestPanel();

        tabs.addTab("Issue Book", issuePanel);
        tabs.addTab("Search Books", searchPanel);
        tabs.addTab("My Books", myBooksPanel);
        tabs.addTab("Check Fines", finePanel);
        tabs.addTab("Request Book", requestPanel);

        studentFrame.add(tabs);
        studentFrame.setVisible(true);
    }
    
    private static JPanel createIssuePanel() {
        JPanel issuePanel = new JPanel();
        issuePanel.setBackground(new Color(0x212121));
        issuePanel.setLayout(null);

        JLabel bookCodeLabel = new JLabel("Enter Book Code: ");
        bookCodeLabel.setForeground(Color.WHITE);
        bookCodeLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        bookCodeLabel.setBounds(50, 60, 150, 40);

        JTextField bookCodeField = new JTextField();
        bookCodeField.setBounds(200, 70, 200, 25);
        bookCodeField.setFont(new Font("Arial", Font.PLAIN, 16));

        JButton issueButton = new JButton("Issue Book");
        issueButton.setBackground(new Color(0x1DB954));
        issueButton.setBounds(420, 70, 120, 25);
        issueButton.setForeground(Color.BLACK);
        issueButton.setFont(new Font("Arial", Font.BOLD, 12));

        // Available books table
        String[] columns = {"Book Code", "Book Name", "Author", "Publisher", "Year", "ISBN", "Available Copies"};
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0);
        JTable bookTable = new JTable(tableModel);
        bookTable.setRowHeight(30);
        bookTable.setFont(new Font("Arial", Font.PLAIN, 12));
        bookTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        bookTable.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(bookTable);
        scrollPane.setBounds(20, 120, 850, 400);

        // Refresh button to reload books
        JButton refreshButton = new JButton("Refresh");
        refreshButton.setBackground(new Color(0x2196F3));
        refreshButton.setBounds(560, 70, 100, 25);
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFont(new Font("Arial", Font.BOLD, 12));

        // Load available books
        loadAvailableBooks(tableModel);

        // Refresh button action
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadAvailableBooks(tableModel);
            }
        });

        // Issue button action
        issueButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String bookCode = bookCodeField.getText().trim();
                if (!bookCode.isEmpty()) {
                    issueBook(bookCode);
                    bookCodeField.setText("");
                    loadAvailableBooks(tableModel);
                } else {
                    JOptionPane.showMessageDialog(issuePanel, "Please enter a book code!");
                }
            }
        });

        issuePanel.add(bookCodeLabel);
        issuePanel.add(bookCodeField);
        issuePanel.add(issueButton);
        issuePanel.add(refreshButton);
        issuePanel.add(scrollPane);

        return issuePanel;
    }
    
    private static JPanel createSearchPanel() {
        JPanel searchPanel = new JPanel();
        searchPanel.setBackground(new Color(0x212121));
        searchPanel.setLayout(null);

        JLabel searchLabel = new JLabel("Search Book by Name: ");
        searchLabel.setForeground(Color.WHITE);
        searchLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        searchLabel.setBounds(50, 30, 180, 40);

        JTextField searchField = new JTextField();
        searchField.setBounds(230, 40, 250, 25);
        searchField.setFont(new Font("Arial", Font.PLAIN, 16));

        JButton searchButton = new JButton("Search");
        searchButton.setBackground(new Color(0x1DB954));
        searchButton.setBounds(500, 40, 100, 25);
        searchButton.setForeground(Color.BLACK);
        searchButton.setFont(new Font("Arial", Font.BOLD, 12));

        JButton showAllButton = new JButton("Show All");
        showAllButton.setBackground(new Color(0x2196F3));
        showAllButton.setBounds(620, 40, 100, 25);
        showAllButton.setForeground(Color.WHITE);
        showAllButton.setFont(new Font("Arial", Font.BOLD, 12));

        // Search results table
        String[] columns = {"Book Code", "Book Name", "Author", "Publisher", "Year", "ISBN", "Total Copies", "Available", "Status"};
        DefaultTableModel searchTableModel = new DefaultTableModel(columns, 0);
        JTable searchTable = new JTable(searchTableModel);
        searchTable.setRowHeight(30);
        searchTable.setFont(new Font("Arial", Font.PLAIN, 12));
        searchTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        searchTable.setBackground(Color.WHITE);

        JScrollPane searchScrollPane = new JScrollPane(searchTable);
        searchScrollPane.setBounds(20, 90, 850, 450);

        // Load all books initially
        searchBooks("", searchTableModel);

        // Search button action
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String searchTerm = searchField.getText().trim();
                searchBooks(searchTerm, searchTableModel);
            }
        });

        // Show all button action
        showAllButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchBooks("", searchTableModel);
                searchField.setText("");
            }
        });

        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(showAllButton);
        searchPanel.add(searchScrollPane);

        return searchPanel;
    }
    
    private static JPanel createMyBooksPanel() {
        JPanel myBooksPanel = new JPanel();
        myBooksPanel.setBackground(new Color(0x212121));
        myBooksPanel.setLayout(null);

        JLabel titleLabel = new JLabel("My Issued Books");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setBounds(350, 20, 200, 30);

        String[] columns = {"Book Code", "Book Name", "Author", "Issue Date", "Due Date", "Days Left", "Status"};
        DefaultTableModel myBooksModel = new DefaultTableModel(columns, 0);
        JTable myBooksTable = new JTable(myBooksModel);
        myBooksTable.setRowHeight(30);
        myBooksTable.setFont(new Font("Arial", Font.PLAIN, 12));
        myBooksTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        myBooksTable.setBackground(Color.WHITE);

        JScrollPane myBooksScrollPane = new JScrollPane(myBooksTable);
        myBooksScrollPane.setBounds(20, 70, 850, 400);

        JButton returnButton = new JButton("Return Selected Book");
        returnButton.setBackground(new Color(0xFF5722));
        returnButton.setBounds(350, 490, 200, 30);
        returnButton.setForeground(Color.WHITE);
        returnButton.setFont(new Font("Arial", Font.BOLD, 12));

        // Load my books
        loadMyBooks(myBooksModel);

        // Return button action
        returnButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = myBooksTable.getSelectedRow();
                if (selectedRow != -1) {
                    String bookCode = (String) myBooksTable.getValueAt(selectedRow, 0);
                    returnBook(bookCode);
                    loadMyBooks(myBooksModel);
                } else {
                    JOptionPane.showMessageDialog(myBooksPanel, "Please select a book to return!");
                }
            }
        });

        myBooksPanel.add(titleLabel);
        myBooksPanel.add(myBooksScrollPane);
        myBooksPanel.add(returnButton);

        return myBooksPanel;
    }
    
    private static JPanel createFinePanel() {
        JPanel finePanel = new JPanel();
        finePanel.setBackground(new Color(0x212121));
        finePanel.setLayout(null);

        JLabel titleLabel = new JLabel("My Fines & Penalties");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setBounds(320, 20, 250, 30);

        String[] columns = {"Book Code", "Book Name", "Due Date", "Days Overdue", "Fine Amount", "Status"};
        DefaultTableModel fineModel = new DefaultTableModel(columns, 0);
        JTable fineTable = new JTable(fineModel);
        fineTable.setRowHeight(30);
        fineTable.setFont(new Font("Arial", Font.PLAIN, 12));
        fineTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        fineTable.setBackground(Color.WHITE);

        JScrollPane fineScrollPane = new JScrollPane(fineTable);
        fineScrollPane.setBounds(20, 70, 850, 350);

        JLabel totalFineLabel = new JLabel("Total Outstanding Fine: $0.00");
        totalFineLabel.setForeground(Color.RED);
        totalFineLabel.setFont(new Font("Arial", Font.BOLD, 16));
        totalFineLabel.setBounds(20, 440, 300, 30);

        JButton payFineButton = new JButton("Pay Fine");
        payFineButton.setBackground(new Color(0x4CAF50));
        payFineButton.setBounds(400, 440, 120, 30);
        payFineButton.setForeground(Color.WHITE);
        payFineButton.setFont(new Font("Arial", Font.BOLD, 12));

        // Load fines
        double totalFine = loadFines(fineModel);
        totalFineLabel.setText("Total Outstanding Fine: $" + String.format("%.2f", totalFine));

        finePanel.add(titleLabel);
        finePanel.add(fineScrollPane);
        finePanel.add(totalFineLabel);
        finePanel.add(payFineButton);

        return finePanel;
    }
    
    private static JPanel createRequestPanel() {
        JPanel requestPanel = new JPanel();
        requestPanel.setBackground(new Color(0x212121));
        requestPanel.setLayout(null);

        JLabel titleLabel = new JLabel("Request New Book");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setBounds(350, 20, 200, 30);

        JLabel bookNameLabel = new JLabel("Book Name:");
        bookNameLabel.setForeground(Color.WHITE);
        bookNameLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        bookNameLabel.setBounds(50, 80, 100, 25);

        JTextField bookNameField = new JTextField();
        bookNameField.setBounds(160, 80, 250, 25);

        JLabel authorLabel = new JLabel("Author:");
        authorLabel.setForeground(Color.WHITE);
        authorLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        authorLabel.setBounds(50, 120, 100, 25);

        JTextField authorField = new JTextField();
        authorField.setBounds(160, 120, 250, 25);

        JLabel publisherLabel = new JLabel("Publisher:");
        publisherLabel.setForeground(Color.WHITE);
        publisherLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        publisherLabel.setBounds(50, 160, 100, 25);

        JTextField publisherField = new JTextField();
        publisherField.setBounds(160, 160, 250, 25);

        JLabel reasonLabel = new JLabel("Reason:");
        reasonLabel.setForeground(Color.WHITE);
        reasonLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        reasonLabel.setBounds(50, 200, 100, 25);

        JTextArea reasonArea = new JTextArea();
        reasonArea.setLineWrap(true);
        reasonArea.setWrapStyleWord(true);
        JScrollPane reasonScrollPane = new JScrollPane(reasonArea);
        reasonScrollPane.setBounds(160, 200, 250, 80);

        JButton submitButton = new JButton("Submit Request");
        submitButton.setBackground(new Color(0x2196F3));
        submitButton.setBounds(200, 300, 150, 30);
        submitButton.setForeground(Color.WHITE);
        submitButton.setFont(new Font("Arial", Font.BOLD, 12));

        // My requests table
        JLabel myRequestsLabel = new JLabel("My Requests");
        myRequestsLabel.setForeground(Color.WHITE);
        myRequestsLabel.setFont(new Font("Arial", Font.BOLD, 16));
        myRequestsLabel.setBounds(50, 350, 150, 25);

        String[] columns = {"Request ID", "Book Code", "Request Date", "Status", "Notes"};
        DefaultTableModel requestModel = new DefaultTableModel(columns, 0);
        JTable requestTable = new JTable(requestModel);
        requestTable.setRowHeight(25);
        requestTable.setFont(new Font("Arial", Font.PLAIN, 11));
        requestTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 11));
        requestTable.setBackground(Color.WHITE);

        JScrollPane requestScrollPane = new JScrollPane(requestTable);
        requestScrollPane.setBounds(50, 380, 800, 150);

        // Submit button action
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String bookName = bookNameField.getText().trim();
                String author = authorField.getText().trim();
                String publisher = publisherField.getText().trim();
                String reason = reasonArea.getText().trim();

                if (!bookName.isEmpty() && !author.isEmpty()) {
                    submitBookRequest(bookName, author, publisher, reason);
                    bookNameField.setText("");
                    authorField.setText("");
                    publisherField.setText("");
                    reasonArea.setText("");
                    loadMyRequests(requestModel);
                } else {
                    JOptionPane.showMessageDialog(requestPanel, "Please fill in at least Book Name and Author!");
                }
            }
        });

        // Load existing requests
        loadMyRequests(requestModel);

        requestPanel.add(titleLabel);
        requestPanel.add(bookNameLabel);
        requestPanel.add(bookNameField);
        requestPanel.add(authorLabel);
        requestPanel.add(authorField);
        requestPanel.add(publisherLabel);
        requestPanel.add(publisherField);
        requestPanel.add(reasonLabel);
        requestPanel.add(reasonScrollPane);
        requestPanel.add(submitButton);
        requestPanel.add(myRequestsLabel);
        requestPanel.add(requestScrollPane);

        return requestPanel;
    }

    // Database operation methods
    private static void loadAvailableBooks(DefaultTableModel model) {
        model.setRowCount(0);
        try {
            if (connection == null || connection.isClosed()) {
                JOptionPane.showMessageDialog(null, "Database connection is not available. Please restart the application.");
                return;
            }
            
            String query = "SELECT book_code, book_name, author, publisher, year_published, isbn, available_copies FROM books WHERE available_copies > 0 ORDER BY book_name";
            PreparedStatement stmt = connection.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            int rowCount = 0;
            while (rs.next()) {
                Object[] row = {
                    rs.getString("book_code"),
                    rs.getString("book_name"),
                    rs.getString("author"),
                    rs.getString("publisher"),
                    rs.getInt("year_published"),
                    rs.getString("isbn"),
                    rs.getInt("available_copies")
                };
                model.addRow(row);
                rowCount++;
            }
            
            if (rowCount == 0) {
                JOptionPane.showMessageDialog(null, "No books available in the database. Please check if sample data has been inserted.");
            }
            
            stmt.close();
            rs.close();
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error loading books: " + e.getMessage());
            e.printStackTrace();
            
            // Try to show all books regardless of availability as fallback
            try {
                String fallbackQuery = "SELECT book_code, book_name, author, publisher, year_published, isbn, available_copies FROM books ORDER BY book_name";
                PreparedStatement fallbackStmt = connection.prepareStatement(fallbackQuery);
                ResultSet fallbackRs = fallbackStmt.executeQuery();
                
                while (fallbackRs.next()) {
                    Object[] row = {
                        fallbackRs.getString("book_code"),
                        fallbackRs.getString("book_name"),
                        fallbackRs.getString("author"),
                        fallbackRs.getString("publisher"),
                        fallbackRs.getInt("year_published"),
                        fallbackRs.getString("isbn"),
                        fallbackRs.getInt("available_copies")
                    };
                    model.addRow(row);
                }
                fallbackStmt.close();
                fallbackRs.close();
            } catch (SQLException ex) {
                System.err.println("Fallback query also failed: " + ex.getMessage());
            }
        }
    }

    private static void searchBooks(String searchTerm, DefaultTableModel model) {
        model.setRowCount(0);
        try {
            String query;
            PreparedStatement stmt;
            
            if (searchTerm.isEmpty()) {
                query = "SELECT book_code, book_name, author, publisher, year_published, isbn, total_copies, available_copies FROM books";
                stmt = connection.prepareStatement(query);
            } else {
                query = "SELECT book_code, book_name, author, publisher, year_published, isbn, total_copies, available_copies FROM books WHERE book_name LIKE ? OR author LIKE ?";
                stmt = connection.prepareStatement(query);
                stmt.setString(1, "%" + searchTerm + "%");
                stmt.setString(2, "%" + searchTerm + "%");
            }
            
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int availableCopies = rs.getInt("available_copies");
                String status = availableCopies > 0 ? "Available" : "Not Available";
                
                Object[] row = {
                    rs.getString("book_code"),
                    rs.getString("book_name"),
                    rs.getString("author"),
                    rs.getString("publisher"),
                    rs.getInt("year_published"),
                    rs.getString("isbn"),
                    rs.getInt("total_copies"),
                    availableCopies,
                    status
                };
                model.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error searching books: " + e.getMessage());
        }
    }

    private static void issueBook(String bookCode) {
        try {
            // First check if student exists
            String studentCheckQuery = "SELECT student_name FROM students WHERE student_id = ?";
            PreparedStatement studentCheckStmt = connection.prepareStatement(studentCheckQuery);
            studentCheckStmt.setString(1, currentStudentId);
            ResultSet studentRs = studentCheckStmt.executeQuery();
            
            if (!studentRs.next()) {
                JOptionPane.showMessageDialog(null, "Student not found. Please contact administrator.");
                return;
            }
            studentCheckStmt.close();
            studentRs.close();
            
            // Check if book is available
            String checkQuery = "SELECT available_copies, book_name FROM books WHERE book_code = ?";
            PreparedStatement checkStmt = connection.prepareStatement(checkQuery);
            checkStmt.setString(1, bookCode);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                int availableCopies = rs.getInt("available_copies");
                String bookName = rs.getString("book_name");
                
                if (availableCopies > 0) {
                    // Check if student already has this book
                    String duplicateCheckQuery = "SELECT COUNT(*) FROM issued_books WHERE student_id = ? AND book_code = ? AND status = 'ACTIVE'";
                    PreparedStatement duplicateStmt = connection.prepareStatement(duplicateCheckQuery);
                    duplicateStmt.setString(1, currentStudentId);
                    duplicateStmt.setString(2, bookCode);
                    ResultSet duplicateRs = duplicateStmt.executeQuery();
                    
                    if (duplicateRs.next() && duplicateRs.getInt(1) > 0) {
                        JOptionPane.showMessageDialog(null, "You have already issued this book!");
                        duplicateStmt.close();
                        duplicateRs.close();
                        return;
                    }
                    duplicateStmt.close();
                    duplicateRs.close();
                    
                    // Issue the book
                    String issueQuery = "INSERT INTO issued_books (student_id, book_code, issue_date, due_date, status) VALUES (?, ?, ?, ?, 'ACTIVE')";
                    PreparedStatement issueStmt = connection.prepareStatement(issueQuery);
                    issueStmt.setString(1, currentStudentId);
                    issueStmt.setString(2, bookCode);
                    issueStmt.setDate(3, Date.valueOf(LocalDate.now()));
                    issueStmt.setDate(4, Date.valueOf(LocalDate.now().plusDays(14))); // 14 days loan period
                    issueStmt.executeUpdate();

                    // Update available copies
                    String updateQuery = "UPDATE books SET available_copies = available_copies - 1 WHERE book_code = ?";
                    PreparedStatement updateStmt = connection.prepareStatement(updateQuery);
                    updateStmt.setString(1, bookCode);
                    updateStmt.executeUpdate();

                    JOptionPane.showMessageDialog(null, "Book '" + bookName + "' issued successfully!\nDue date: " + LocalDate.now().plusDays(14));
                    
                    issueStmt.close();
                    updateStmt.close();
                } else {
                    JOptionPane.showMessageDialog(null, "Book is not available for issue.");
                }
            } else {
                JOptionPane.showMessageDialog(null, "Book code not found!");
            }
            
            checkStmt.close();
            rs.close();
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error issuing book: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void loadMyBooks(DefaultTableModel model) {
        model.setRowCount(0);
        try {
            String query = "SELECT ib.book_code, b.book_name, b.author, ib.issue_date, ib.due_date, ib.status " +
                          "FROM issued_books ib JOIN books b ON ib.book_code = b.book_code " +
                          "WHERE ib.student_id = ? AND ib.status = 'ACTIVE'";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, currentStudentId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Date dueDate = rs.getDate("due_date");
                LocalDate due = dueDate.toLocalDate();
                LocalDate today = LocalDate.now();
                long daysLeft = java.time.temporal.ChronoUnit.DAYS.between(today, due);
                
                String status = daysLeft < 0 ? "OVERDUE" : (daysLeft <= 3 ? "DUE SOON" : "ACTIVE");
                
                Object[] row = {
                    rs.getString("book_code"),
                    rs.getString("book_name"),
                    rs.getString("author"),
                    rs.getDate("issue_date").toString(),
                    rs.getDate("due_date").toString(),
                    daysLeft + " days",
                    status
                };
                model.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error loading my books: " + e.getMessage());
        }
    }

    private static void returnBook(String bookCode) {
        try {
            // Update issued_books status
            String updateQuery = "UPDATE issued_books SET status = 'RETURNED', return_date = ? WHERE student_id = ? AND book_code = ? AND status = 'ACTIVE'";
            PreparedStatement updateStmt = connection.prepareStatement(updateQuery);
            updateStmt.setDate(1, Date.valueOf(LocalDate.now()));
            updateStmt.setString(2, currentStudentId);
            updateStmt.setString(3, bookCode);
            updateStmt.executeUpdate();

            // Update available copies
            String updateCopiesQuery = "UPDATE books SET available_copies = available_copies + 1 WHERE book_code = ?";
            PreparedStatement updateCopiesStmt = connection.prepareStatement(updateCopiesQuery);
            updateCopiesStmt.setString(1, bookCode);
            updateCopiesStmt.executeUpdate();

            JOptionPane.showMessageDialog(null, "Book returned successfully!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error returning book: " + e.getMessage());
        }
    }

    private static double loadFines(DefaultTableModel model) {
        model.setRowCount(0);
        double totalFine = 0.0;
        try {
            String query = "SELECT ib.book_code, b.book_name, ib.due_date, " +
                          "DATEDIFF(CURDATE(), ib.due_date) as days_overdue " +
                          "FROM issued_books ib JOIN books b ON ib.book_code = b.book_code " +
                          "WHERE ib.student_id = ? AND ib.status = 'ACTIVE' AND ib.due_date < CURDATE()";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, currentStudentId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int daysOverdue = rs.getInt("days_overdue");
                double fineAmount = daysOverdue * 0.50; // $0.50 per day
                totalFine += fineAmount;
                
                Object[] row = {
                    rs.getString("book_code"),
                    rs.getString("book_name"),
                    rs.getDate("due_date").toString(),
                    daysOverdue + " days",
                    "$" + String.format("%.2f", fineAmount),
                    "UNPAID"
                };
                model.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error loading fines: " + e.getMessage());
        }
        return totalFine;
    }

    private static void submitBookRequest(String bookName, String author, String publisher, String reason) {
    try {
        // First find the book by name to get its code
        String findBookQuery = "SELECT book_code FROM books WHERE book_name = ? AND author = ?";
        PreparedStatement findStmt = connection.prepareStatement(findBookQuery);
        findStmt.setString(1, bookName);
        findStmt.setString(2, author);
        ResultSet rs = findStmt.executeQuery();
        
        if (rs.next()) {
            String bookCode = rs.getString("book_code");
            String query = "INSERT INTO book_requests (student_id, book_code, request_date, status, notes) " +
                         "VALUES (?, ?, ?, 'PENDING', ?)";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, currentStudentId);
            stmt.setString(2, bookCode);
            stmt.setDate(3, Date.valueOf(LocalDate.now()));
            stmt.setString(4, reason);
            stmt.executeUpdate();
            
            JOptionPane.showMessageDialog(null, "Book request submitted successfully!");
        } else {
            JOptionPane.showMessageDialog(null, "Book not found in catalog. Please request a different book.");
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(null, "Error submitting request: " + e.getMessage());
    }
}

    private static void loadMyRequests(DefaultTableModel model) {
    model.setRowCount(0);
    try {
        String query = "SELECT r.request_id, b.book_name, b.author, b.publisher, r.request_date, r.status " +
                      "FROM book_requests r JOIN books b ON r.book_code = b.book_code " +
                      "WHERE r.student_id = ? ORDER BY r.request_date DESC";
        PreparedStatement stmt = connection.prepareStatement(query);
        stmt.setString(1, currentStudentId);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            Object[] row = {
                rs.getInt("request_id"),
                rs.getString("book_name"),
                rs.getString("author"),
                rs.getString("publisher"),
                rs.getDate("request_date").toString(),
                rs.getString("status")
            };
            model.addRow(row);
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(null, "Error loading requests: " + e.getMessage());
    }
}
}