package com.reservation.dao;

import com.reservation.db.DatabaseManager;
import com.reservation.model.User;
import com.reservation.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDao {

    public User authenticate(String username, String rawPassword) {
        if (username == null || rawPassword == null) {
            return null;
        }

        String sql = "SELECT id, username, password_hash, full_name, role FROM users WHERE username = ?;";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username.trim().toLowerCase());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password_hash");
                    if (PasswordUtil.verifyPassword(rawPassword, storedHash)) {
                        return new User(
                                rs.getInt("id"),
                                rs.getString("username"),
                                storedHash,
                                rs.getString("full_name"),
                                rs.getString("role")
                        );
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Authentication query error: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public boolean register(String username, String rawPassword, String fullName) {
        if (username == null || rawPassword == null || fullName == null) {
            return false;
        }

        String sql = "INSERT INTO users (username, password_hash, full_name, role) VALUES (?, ?, ?, 'USER');";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username.trim().toLowerCase());
            pstmt.setString(2, PasswordUtil.hashPassword(rawPassword));
            pstmt.setString(3, fullName.trim());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Registration error: " + e.getMessage());
            return false;
        }
    }

    public boolean usernameExists(String username) {
        if (username == null) return false;
        String sql = "SELECT 1 FROM users WHERE username = ?;";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username.trim().toLowerCase());
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }
}

