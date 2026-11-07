package com.reservation.dao;

import com.reservation.db.DatabaseManager;
import com.reservation.model.Reservation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ReservationDao {

    public boolean createReservation(Reservation res) {
        String sql = "INSERT INTO reservations (pnr, passenger_name, train_number, train_name, " +
                "class_type, journey_date, source, destination, booking_date, status, booked_by) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, res.getPnr());
            pstmt.setString(2, res.getPassengerName());
            pstmt.setInt(3, res.getTrainNumber());
            pstmt.setString(4, res.getTrainName());
            pstmt.setString(5, res.getClassType());
            pstmt.setString(6, res.getJourneyDate());
            pstmt.setString(7, res.getSource());
            pstmt.setString(8, res.getDestination());
            pstmt.setString(9, res.getBookingDate());
            pstmt.setString(10, res.getStatus());
            pstmt.setString(11, res.getBookedBy());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error creating reservation: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public Reservation findByPNR(String pnr) {
        if (pnr == null || pnr.trim().isEmpty()) {
            return null;
        }

        String sql = "SELECT pnr, passenger_name, train_number, train_name, class_type, " +
                "journey_date, source, destination, booking_date, status, booked_by " +
                "FROM reservations WHERE UPPER(TRIM(pnr)) = UPPER(TRIM(?));";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, pnr.trim());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractReservation(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching reservation by PNR: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public boolean deleteByPNR(String pnr) {
        if (pnr == null || pnr.trim().isEmpty()) {
            return false;
        }

        String sql = "DELETE FROM reservations WHERE UPPER(TRIM(pnr)) = UPPER(TRIM(?));";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, pnr.trim());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting reservation: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean isPNRExists(String pnr) {
        if (pnr == null || pnr.trim().isEmpty()) {
            return false;
        }

        String sql = "SELECT 1 FROM reservations WHERE UPPER(TRIM(pnr)) = UPPER(TRIM(?));";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, pnr.trim());
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }

    public List<Reservation> getAllReservations() {
        List<Reservation> list = new ArrayList<>();
        String sql = "SELECT pnr, passenger_name, train_number, train_name, class_type, " +
                "journey_date, source, destination, booking_date, status, booked_by " +
                "FROM reservations ORDER BY booking_date DESC;";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(extractReservation(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting all reservations: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    public List<Reservation> getReservationsByUser(String username) {
        List<Reservation> list = new ArrayList<>();
        String sql = "SELECT pnr, passenger_name, train_number, train_name, class_type, " +
                "journey_date, source, destination, booking_date, status, booked_by " +
                "FROM reservations WHERE booked_by = ? ORDER BY booking_date DESC;";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(extractReservation(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting user reservations: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    private Reservation extractReservation(ResultSet rs) throws SQLException {
        return new Reservation(
                rs.getString("pnr"),
                rs.getString("passenger_name"),
                rs.getInt("train_number"),
                rs.getString("train_name"),
                rs.getString("class_type"),
                rs.getString("journey_date"),
                rs.getString("source"),
                rs.getString("destination"),
                rs.getString("booking_date"),
                rs.getString("status"),
                rs.getString("booked_by")
        );
    }
}

