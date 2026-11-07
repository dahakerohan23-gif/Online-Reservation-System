package com.reservation.db;

import com.reservation.util.PasswordUtil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:reservation.db";

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC Driver not found in classpath!");
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(DB_URL);
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
        }
        return conn;
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection()) {
            createTables(conn);
            seedDefaultUsers(conn);
            seedDefaultTrains(conn);
            System.out.println("Database initialized successfully.");
        } catch (SQLException e) {
            System.err.println("Database initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void createTables(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // Users table
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "username TEXT UNIQUE NOT NULL, " +
                    "password_hash TEXT NOT NULL, " +
                    "full_name TEXT NOT NULL, " +
                    "role TEXT NOT NULL DEFAULT 'USER'" +
                    ");");

            // Trains table
            stmt.execute("CREATE TABLE IF NOT EXISTS trains (" +
                    "train_number INTEGER PRIMARY KEY, " +
                    "train_name TEXT NOT NULL, " +
                    "source TEXT NOT NULL, " +
                    "destination TEXT NOT NULL" +
                    ");");

            // Reservations table
            stmt.execute("CREATE TABLE IF NOT EXISTS reservations (" +
                    "pnr TEXT PRIMARY KEY, " +
                    "passenger_name TEXT NOT NULL, " +
                    "train_number INTEGER NOT NULL, " +
                    "train_name TEXT NOT NULL, " +
                    "class_type TEXT NOT NULL, " +
                    "journey_date TEXT NOT NULL, " +
                    "source TEXT NOT NULL, " +
                    "destination TEXT NOT NULL, " +
                    "booking_date TEXT NOT NULL, " +
                    "status TEXT NOT NULL DEFAULT 'CONFIRMED', " +
                    "booked_by TEXT NOT NULL" +
                    ");");
        }
    }

    private static void seedDefaultUsers(Connection conn) throws SQLException {
        String countSql = "SELECT COUNT(*) FROM users;";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(countSql)) {
            if (rs.next() && rs.getInt(1) == 0) {
                String insertSql = "INSERT INTO users (username, password_hash, full_name, role) VALUES (?, ?, ?, ?);";
                try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                    // Admin user
                    pstmt.setString(1, "admin");
                    pstmt.setString(2, PasswordUtil.hashPassword("admin123"));
                    pstmt.setString(3, "System Administrator");
                    pstmt.setString(4, "ADMIN");
                    pstmt.executeUpdate();

                    // Regular user
                    pstmt.setString(1, "user");
                    pstmt.setString(2, PasswordUtil.hashPassword("user123"));
                    pstmt.setString(3, "Demo Passenger");
                    pstmt.setString(4, "USER");
                    pstmt.executeUpdate();

                    // Another test user
                    pstmt.setString(1, "rohan");
                    pstmt.setString(2, PasswordUtil.hashPassword("pass123"));
                    pstmt.setString(3, "Rohan Sharma");
                    pstmt.setString(4, "USER");
                    pstmt.executeUpdate();

                    System.out.println("Default users seeded.");
                }
            }
        }
    }

    private static void seedDefaultTrains(Connection conn) throws SQLException {
        String countSql = "SELECT COUNT(*) FROM trains;";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(countSql)) {
            if (rs.next() && rs.getInt(1) == 0) {
                String insertSql = "INSERT INTO trains (train_number, train_name, source, destination) VALUES (?, ?, ?, ?);";
                try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                    Object[][] trains = {
                            {12001, "Shatabdi Express", "New Delhi", "Bhopal Junction"},
                            {12951, "Mumbai Rajdhani Express", "Mumbai Central", "New Delhi"},
                            {22436, "Vande Bharat Express", "New Delhi", "Varanasi Junction"},
                            {12259, "Sealdah Bikaner Duronto", "Sealdah", "Bikaner Junction"},
                            {12626, "Kerala Superfast Express", "New Delhi", "Thiruvananthapuram Central"},
                            {12138, "Punjab Mail", "Firozpur Cantt", "Mumbai CSMT"},
                            {12301, "Howrah Rajdhani Express", "Howrah Junction", "New Delhi"},
                            {12616, "Grand Trunk Express", "New Delhi", "MGR Chennai Central"},
                            {20608, "Mysuru Vande Bharat", "Mysuru Junction", "MGR Chennai Central"},
                            {12009, "Mumbai Ahmedabad Shatabdi", "Mumbai Central", "Ahmedabad Junction"},
                            {12424, "Dibrugarh Rajdhani", "New Delhi", "Dibrugarh"},
                            {12724, "Telangana Express", "New Delhi", "Hyderabad Deccan"}
                    };

                    for (Object[] t : trains) {
                        pstmt.setInt(1, (Integer) t[0]);
                        pstmt.setString(2, (String) t[1]);
                        pstmt.setString(3, (String) t[2]);
                        pstmt.setString(4, (String) t[3]);
                        pstmt.executeUpdate();
                    }
                    System.out.println("Default trains seeded.");
                }
            }
        }
    }
}

