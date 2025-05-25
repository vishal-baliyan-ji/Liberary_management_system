// LibraryManagementSystem.java (with MySQL Integration)
import java.sql.*;
import java.util.*;

public class LibraryManagementSystem {

    static class DBUtil {
        private static final String URL = "jdbc:mysql://localhost:3306/library_db";
        private static final String USER = "root";
        private static final String PASS = "password";

        public static Connection getConnection() throws SQLException {
            return DriverManager.getConnection(URL, USER, PASS);
        }
    }

    static class Person {
        protected int id;
        protected String name;

        public Person(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    static class User extends Person {
        private String password;
        private List<Book> borrowedBooks;

        public User(int id, String name, String password) {
            super(id, name);
            this.password = password;
            this.borrowedBooks = new ArrayList<>();
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public boolean checkPassword(String password) { return this.password.equals(password); }
        public void borrowBook(Book book) {
            borrowedBooks.add(book);
            BorrowDAO.addBorrowedBook(id, book.getId());
        }
        public void returnBook(Book book) {
            borrowedBooks.remove(book);
            BorrowDAO.removeBorrowedBook(id, book.getId());
        }
        public List<Book> getBorrowedBooks() { return borrowedBooks; }
    }

    static class Book {
        private int id;
        private String title;
        private String author;
        private boolean isIssued;

        public Book(int id, String title, String author) {
            this.id = id;
            this.title = title;
            this.author = author;
            this.isIssued = false;
        }

        public int getId() { return id; }
        public String getTitle() { return title; }
        public String getAuthor() { return author; }
        public boolean isIssued() { return isIssued; }
        public void setIssued(boolean issued) { this.isIssued = issued; }
        public void showInfo() {
            System.out.println(id + ": " + title + " by " + author + (isIssued ? " (Issued)" : ""));
        }
    }

    static class BookDAO {
        public static List<Book> loadBooks() {
            List<Book> books = new ArrayList<>();
            try (Connection conn = DBUtil.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM books")) {
                while (rs.next()) {
                    Book book = new Book(rs.getInt("id"), rs.getString("title"), rs.getString("author"));
                    book.setIssued(rs.getBoolean("is_issued"));
                    books.add(book);
                }
            } catch (SQLException e) {
                System.out.println("Error loading books: " + e.getMessage());
            }
            return books;
        }

        public static void saveBook(Book book) {
            try (Connection conn = DBUtil.getConnection();
                 PreparedStatement ps = conn.prepareStatement("REPLACE INTO books (id, title, author, is_issued) VALUES (?, ?, ?, ?)");) {
                ps.setInt(1, book.getId());
                ps.setString(2, book.getTitle());
                ps.setString(3, book.getAuthor());
                ps.setBoolean(4, book.isIssued());
                ps.executeUpdate();
            } catch (SQLException e) {
                System.out.println("Error saving book: " + e.getMessage());
            }
        }
    }

    static class UserDAO {
        public static List<User> loadUsers() {
            List<User> users = new ArrayList<>();
            try (Connection conn = DBUtil.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM users")) {
                while (rs.next()) {
                    users.add(new User(rs.getInt("id"), rs.getString("name"), rs.getString("password")));
                }
            } catch (SQLException e) {
                System.out.println("Error loading users: " + e.getMessage());
            }
            return users;
        }

        public static void saveUser(User user) {
            try (Connection conn = DBUtil.getConnection();
                 PreparedStatement ps = conn.prepareStatement("REPLACE INTO users (id, name, password) VALUES (?, ?, ?)");) {
                ps.setInt(1, user.getId());
                ps.setString(2, user.getName());
                ps.setString(3, user.password);
                ps.executeUpdate();
            } catch (SQLException e) {
                System.out.println("Error saving user: " + e.getMessage());
            }
        }
    }

    static class BorrowDAO {
        public static void addBorrowedBook(int userId, int bookId) {
            try (Connection conn = DBUtil.getConnection();
                 PreparedStatement ps = conn.prepareStatement("INSERT INTO borrowed_books (user_id, book_id) VALUES (?, ?)");) {
                ps.setInt(1, userId);
                ps.setInt(2, bookId);
                ps.executeUpdate();
            } catch (SQLException e) {
                System.out.println("Error issuing book: " + e.getMessage());
            }
        }

        public static void removeBorrowedBook(int userId, int bookId) {
            try (Connection conn = DBUtil.getConnection();
                 PreparedStatement ps = conn.prepareStatement("DELETE FROM borrowed_books WHERE user_id=? AND book_id=?");) {
                ps.setInt(1, userId);
                ps.setInt(2, bookId);
                ps.executeUpdate();
            } catch (SQLException e) {
                System.out.println("Error returning book: " + e.getMessage());
            }
        }
    }

    static class LibrarySystem {
        List<Book> books;
        List<User> users;
        User currentUser;
        int nextBookId = 1;

        public LibrarySystem() {
            books = BookDAO.loadBooks();
            users = UserDAO.loadUsers();
            for (Book b : books) if (b.getId() >= nextBookId) nextBookId = b.getId() + 1;
        }

        public void registerUser(Scanner sc) {
            System.out.print("Enter new user ID: ");
            int newId = sc.nextInt(); sc.nextLine();
            System.out.print("Enter your name: ");
            String name = sc.nextLine();
            System.out.print("Enter a password: ");
            String password = sc.nextLine();
            for (User user : users) if (user.getId() == newId) {
                System.out.println("User ID already exists."); return; }
            User newUser = new User(newId, name, password);
            users.add(newUser);
            UserDAO.saveUser(newUser);
            System.out.println("Registration successful.");
        }

        public void login(Scanner sc) {
            System.out.print("Enter user ID: ");
            int userId = sc.nextInt(); sc.nextLine();
            System.out.print("Enter password: ");
            String pwd = sc.nextLine();
            for (User user : users) if (user.getId() == userId && user.checkPassword(pwd)) {
                currentUser = user;
                System.out.println("Login successful. Welcome, " + user.getName());
                return;
            }
            System.out.println("Invalid credentials.");
        }

        public void searchBook(Scanner sc) {
            sc.nextLine();
            System.out.print("Enter book title or author to search: ");
            String query = sc.nextLine().toLowerCase();
            boolean found = false;
            for (Book book : books) {
                if (book.getTitle().toLowerCase().contains(query) ||
                    book.getAuthor().toLowerCase().contains(query)) {
                    book.showInfo(); found = true;
                }
            }
            if (!found) System.out.println("No book found.");
        }

        public void issueBook(Scanner sc) {
            if (currentUser == null) {
                System.out.println("Login first."); return;
            }
            sc.nextLine();
            System.out.print("Enter book title to issue: ");
            String title = sc.nextLine();
            for (Book book : books) if (book.getTitle().equalsIgnoreCase(title)) {
                if (!book.isIssued()) {
                    book.setIssued(true);
                    currentUser.borrowBook(book);
                    BookDAO.saveBook(book);
                    System.out.println("Book issued.");
                } else System.out.println("Book is already issued.");
                return;
            }
            System.out.print("Book not found. Add and issue it? (yes/no): ");
            if (sc.nextLine().equalsIgnoreCase("yes")) {
                System.out.print("Enter author: ");
                String author = sc.nextLine();
                Book newBook = new Book(nextBookId++, title, author);
                newBook.setIssued(true);
                books.add(newBook);
                currentUser.borrowBook(newBook);
                BookDAO.saveBook(newBook);
                System.out.println("Book added and issued.");
            }
        }

        public void returnBook(Scanner sc) {
            if (currentUser == null) {
                System.out.println("Login first."); return;
            }
            sc.nextLine();
            System.out.print("Enter book title to return: ");
            String title = sc.nextLine();
            for (Book book : currentUser.getBorrowedBooks()) {
                if (book.getTitle().equalsIgnoreCase(title)) {
                    book.setIssued(false);
                    currentUser.returnBook(book);
                    BookDAO.saveBook(book);
                    System.out.println("Book returned."); return;
                }
            }
            System.out.println("Book not in your list.");
        }

        public void generateReport() {
            if (currentUser == null) {
                System.out.println("Login first."); return;
            }
            System.out.println("=== Report for " + currentUser.getName() + " ===");
            if (!currentUser.getBorrowedBooks().isEmpty()) {
                for (Book b : currentUser.getBorrowedBooks()) System.out.println("- " + b.getTitle());
            } else System.out.println("No books borrowed.");
        }

        public void addBook(Scanner sc) {
            if (currentUser == null) {
                System.out.println("Login first."); return;
            }
            sc.nextLine();
            System.out.print("Enter book title: ");
            String title = sc.nextLine();
            System.out.print("Enter author: ");
            String author = sc.nextLine();
            Book book = new Book(nextBookId++, title, author);
            books.add(book);
            BookDAO.saveBook(book);
            System.out.println("Book added.");
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        LibrarySystem lib = new LibrarySystem();
        int choice = -1;

        System.out.println("=== GUVI Library Management System ===");

        do {
            System.out.println("\nMenu:");
            System.out.println("0. Register");
            System.out.println("1. Login");
            System.out.println("2. Search Book");
            System.out.println("3. Issue Book");
            System.out.println("4. Return Book");
            System.out.println("5. Generate Report");
            System.out.println("6. Add Book");
            System.out.println("7. Exit");
            System.out.print("Choose an option: ");

            if (scanner.hasNextInt()) {
                choice = scanner.nextInt();
            } else {
                System.out.println("Invalid input.");
                scanner.next();
                continue;
            }

            switch (choice) {
                case 0 -> lib.registerUser(scanner);
                case 1 -> lib.login(scanner);
                case 2 -> lib.searchBook(scanner);
                case 3 -> lib.issueBook(scanner);
                case 4 -> lib.returnBook(scanner);
                case 5 -> lib.generateReport();
                case 6 -> lib.addBook(scanner);
                case 7 -> System.out.println("Exiting...");
                default -> System.out.println("Invalid option.");
            }

        } while (choice != 7);

        scanner.close();
    }
}
