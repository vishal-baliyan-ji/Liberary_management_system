


# Library Management System

## Project Overview

This is a Java-based Library Management System that provides functionality for both librarians and students. The system includes:

- Librarian Panel: For managing books, registering students, and viewing fines
- Student Portal: For searching and issuing books, viewing issued books, checking fines, and requesting new books

## Project Structure
````markdown
<pre>
Library_management/
├── src/
│   └── Library_management/
│       ├── Library_management.java     # Main application with login system
│       ├── LibrarianSystem.java        # Librarian management interface
│       └── StudentSection.java         # Student portal interface
├── db/
│   └── library_management.sql          # MySQL schema and data
├── lib/
│   └── mysql-connector-j-8.0.xx.jar    # MySQL JDBC driver
└── README.md
</pre>
````
## Database Configuration

The database files are located in the `db` folder. The system uses MySQL with the following configuration:

- Database name: `library_management`
- Default username: `root`
- Default password: `Squard@502` (you should change this)

## Database Schema

The system uses the following MySQL tables:

- `students` – Stores student information
- `students_auth` – Stores student login credentials
- `librarians` – Stores librarian accounts
- `books` – Contains book information
- `issued_books` – Tracks book loans
- `book_requests` – Manages book requests from students

## Setup Instructions

### Database Setup

1. Import the SQL file `library_management.sql` from the `db` folder into your MySQL server.
2. Change the database password in all Java files. Search for `Squard@502` and replace it with your actual MySQL password.

### Dependencies

- Java 8 or higher
- MySQL Connector/J (should be in the `lib` folder)

### Compiling and Running the Application

1. Open a terminal or command prompt.
2. Compile all `.java` files:

   ```bash
   javac -cp ".;lib/mysql-connector-j-8.0.xx.jar" src/Library_management/*.java -d bin
   ```


3. Run the main application:

   ```bash
   java -cp ".;bin;lib/mysql-connector-j-8.0.xx.jar" Library_management.Library_management
   ```

## Changing Database Password

To change the database password:

1. Locate all database connection strings in the following files:

   * `Library_management.java`
   * `LibrarianSystem.java`
   * `StudentSection.java`

2. Replace `"Squard@502"` with your new password in lines like:

   ```java
   conn = DriverManager.getConnection(
       "jdbc:mysql://localhost:3306/library_management", 
       "root", "YOUR_NEW_PASSWORD"
   );
   ```

3. Make sure to update the password in your MySQL user account as well.

## Features

### Librarian Features

* Register new students
* Add, update, or delete books
* View and manage fines

### Student Features

* Search for books
* Issue and return books
* View currently issued books
* Check fines
* Request new books

## Troubleshooting

If you encounter connection issues:

* Verify that the MySQL server is running
* Check that the username and password in the Java code are correct
* Ensure that the `library_management` database has been imported
* Confirm that the MySQL Connector/J `.jar` file is in your classpath

## Security Note

For production use:

* Never use default credentials
* Implement password hashing for storing passwords securely
* Use environment variables or configuration files for sensitive data
* Sanitize all user inputs to prevent SQL injection
* Implement proper role-based access control

## License

This system is intended for educational purposes only. Additional security and performance measures are required before using it in a production environment.

```


