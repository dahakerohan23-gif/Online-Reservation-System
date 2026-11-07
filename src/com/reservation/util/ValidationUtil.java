package com.reservation.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class ValidationUtil {

    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static boolean isPositiveInteger(String str) {
        if (isNullOrEmpty(str)) {
            return false;
        }
        try {
            int val = Integer.parseInt(str.trim());
            return val > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValidJourneyDate(String dateStr) {
        if (isNullOrEmpty(dateStr)) {
            return false;
        }
        try {
            LocalDate date = LocalDate.parse(dateStr.trim(), DATE_FORMATTER);
            LocalDate today = LocalDate.now();
            // Journey date must be today or in the future
            return !date.isBefore(today);
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    public static LocalDate parseDate(String dateStr) {
        if (isNullOrEmpty(dateStr)) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr.trim(), DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}

