package com.reservation.dao;

import com.reservation.db.DatabaseManager;
import com.reservation.model.Train;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class TrainDao {

    public Train findByNumber(int trainNumber) {
        String sql = "SELECT train_number, train_name, source, destination FROM trains WHERE train_number = ?;";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, trainNumber);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Train(
                            rs.getInt("train_number"),
                            rs.getString("train_name"),
                            rs.getString("source"),
                            rs.getString("destination")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Train lookup error: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public List<Train> getAllTrains() {
        List<Train> list = new ArrayList<>();
        String sql = "SELECT train_number, train_name, source, destination FROM trains ORDER BY train_number ASC;";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new Train(
                        rs.getInt("train_number"),
                        rs.getString("train_name"),
                        rs.getString("source"),
                        rs.getString("destination")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching trains: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }
}

