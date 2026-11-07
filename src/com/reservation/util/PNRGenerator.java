package com.reservation.util;

import java.security.SecureRandom;

public class PNRGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Generates a unique 10-digit PNR number.
     * The first digit is between 1-9 so it's always a full 10-digit number.
     */
    public static String generatePNR() {
        StringBuilder pnr = new StringBuilder(10);
        // First digit: 1-9
        pnr.append(RANDOM.nextInt(9) + 1);
        // Next 9 digits: 0-9
        for (int i = 0; i < 9; i++) {
            pnr.append(RANDOM.nextInt(10));
        }
        return pnr.toString();
    }
}

