import com.reservation.db.DatabaseManager;
import com.reservation.dao.ReservationDao;
import com.reservation.dao.TrainDao;
import com.reservation.dao.UserDao;
import com.reservation.model.Reservation;
import com.reservation.model.Train;
import com.reservation.model.User;
import com.reservation.util.PNRGenerator;
import com.reservation.util.ValidationUtil;

import java.time.LocalDate;
import java.util.List;

public class TestSystem {

    private static int testsRun = 0;
    private static int testsPassed = 0;

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("  RUNNING ONLINE RESERVATION SYSTEM TEST SUITE   ");
        System.out.println("=================================================");

        // 1. Initialize Database
        DatabaseManager.initializeDatabase();

        // 2. Test User Authentication & Access Control
        testAuthentication();

        // 3. Test Train Directory & Auto-population
        testTrainLookup();

        // 4. Test Input Validations
        testValidation();

        // 5. Test PNR Generation
        testPNRGeneration();

        // 6. Test Reservation Creation, Fetch, and Cancellation
        testReservationLifecycle();

        System.out.println("=================================================");
        System.out.printf("  SUMMARY: %d/%d TESTS PASSED\n", testsPassed, testsRun);
        System.out.println("=================================================");

        if (testsPassed != testsRun) {
            System.exit(1);
        }
    }

    private static void assertTrue(String testName, boolean condition) {
        testsRun++;
        if (condition) {
            testsPassed++;
            System.out.println("  [PASS] " + testName);
        } else {
            System.err.println("  [FAIL] " + testName);
        }
    }

    private static void testAuthentication() {
        System.out.println("\n--- Testing Authentication & Access Control ---");
        UserDao userDao = new UserDao();

        // Valid Admin login
        User admin = userDao.authenticate("admin", "admin123");
        assertTrue("Admin login with valid password succeeds", admin != null && admin.isAdmin());

        // Valid Regular user login
        User user = userDao.authenticate("rohan", "pass123");
        assertTrue("User 'rohan' login succeeds", user != null && "rohan".equals(user.getUsername()));

        // Invalid password -> Access Denied
        User invalidPass = userDao.authenticate("admin", "wrongpassword");
        assertTrue("Invalid password rejected (access denied)", invalidPass == null);

        // Invalid username -> Access Denied
        User invalidUser = userDao.authenticate("nonexistent_user_99", "admin123");
        assertTrue("Nonexistent user rejected (access denied)", invalidUser == null);

        // Register new user test
        String testUser = "testuser_" + System.currentTimeMillis();
        boolean regSuccess = userDao.register(testUser, "secret123", "Test Passenger");
        assertTrue("New user registration succeeds", regSuccess);

        User registered = userDao.authenticate(testUser, "secret123");
        assertTrue("Newly registered user can authenticate", registered != null && testUser.equals(registered.getUsername()));
    }

    private static void testTrainLookup() {
        System.out.println("\n--- Testing Train Directory & Auto-Population ---");
        TrainDao trainDao = new TrainDao();

        Train t12001 = trainDao.findByNumber(12001);
        assertTrue("Train 12001 found", t12001 != null);
        assertTrue("Train 12001 auto-populated name is Shatabdi Express",
                t12001 != null && "Shatabdi Express".equals(t12001.getTrainName()));
        assertTrue("Train 12001 source is New Delhi",
                t12001 != null && "New Delhi".equals(t12001.getSource()));

        Train t22436 = trainDao.findByNumber(22436);
        assertTrue("Train 22436 is Vande Bharat Express",
                t22436 != null && "Vande Bharat Express".equals(t22436.getTrainName()));

        Train notFound = trainDao.findByNumber(999999);
        assertTrue("Non-existent train number returns null", notFound == null);

        List<Train> allTrains = trainDao.getAllTrains();
        assertTrue("Train directory contains multiple scheduled trains", allTrains.size() >= 10);
    }

    private static void testValidation() {
        System.out.println("\n--- Testing Input Validation ---");

        assertTrue("Empty string detected as empty", ValidationUtil.isNullOrEmpty("   "));
        assertTrue("Non-empty string detected", !ValidationUtil.isNullOrEmpty("Rohan"));

        assertTrue("Valid numeric train number recognized", ValidationUtil.isPositiveInteger("12001"));
        assertTrue("Alpha train number rejected", !ValidationUtil.isPositiveInteger("1200A"));
        assertTrue("Negative number rejected", !ValidationUtil.isPositiveInteger("-5"));
        assertTrue("Zero rejected", !ValidationUtil.isPositiveInteger("0"));

        String today = LocalDate.now().format(ValidationUtil.DATE_FORMATTER);
        String tomorrow = LocalDate.now().plusDays(1).format(ValidationUtil.DATE_FORMATTER);
        String yesterday = LocalDate.now().minusDays(1).format(ValidationUtil.DATE_FORMATTER);

        assertTrue("Today's date is valid for journey", ValidationUtil.isValidJourneyDate(today));
        assertTrue("Future date is valid for journey", ValidationUtil.isValidJourneyDate(tomorrow));
        assertTrue("Past date is rejected for journey", !ValidationUtil.isValidJourneyDate(yesterday));
        assertTrue("Malformed date is rejected", !ValidationUtil.isValidJourneyDate("2026/11/07"));
        assertTrue("Invalid calendar date rejected", !ValidationUtil.isValidJourneyDate("2026-02-31"));
    }

    private static void testPNRGeneration() {
        System.out.println("\n--- Testing PNR Generation ---");

        String pnr1 = PNRGenerator.generatePNR();
        String pnr2 = PNRGenerator.generatePNR();

        assertTrue("PNR is 10 characters long", pnr1.length() == 10);
        assertTrue("PNR is purely numeric", pnr1.matches("^\\d{10}$"));
        assertTrue("Consecutive PNRs are unique", !pnr1.equals(pnr2));
    }

    private static void testReservationLifecycle() {
        System.out.println("\n--- Testing Reservation Creation, Fetch, and Cancellation ---");
        ReservationDao dao = new ReservationDao();

        String pnr = PNRGenerator.generatePNR();
        String journeyDate = LocalDate.now().plusDays(3).format(ValidationUtil.DATE_FORMATTER);

        Reservation newBooking = new Reservation(
                pnr,
                "Aarav Patel",
                12951,
                "Mumbai Rajdhani Express",
                "2A - AC 2-Tier",
                journeyDate,
                "Mumbai Central",
                "New Delhi",
                "2026-11-07 16:30:00",
                "CONFIRMED",
                "rohan"
        );

        // 1. Insert/Book Reservation
        boolean booked = dao.createReservation(newBooking);
        assertTrue("Reservation saved to database", booked);
        assertTrue("isPNRExists returns true for active booking", dao.isPNRExists(pnr));

        // 2. Fetch by PNR
        Reservation fetched = dao.findByPNR(pnr);
        assertTrue("Fetch booking by PNR returns correct record", fetched != null);
        assertTrue("Fetched passenger name matches", fetched != null && "Aarav Patel".equals(fetched.getPassengerName()));
        assertTrue("Fetched train name matches", fetched != null && "Mumbai Rajdhani Express".equals(fetched.getTrainName()));
        assertTrue("Fetched class type matches", fetched != null && "2A - AC 2-Tier".equals(fetched.getClassType()));
        assertTrue("Fetched route matches", fetched != null && "Mumbai Central".equals(fetched.getSource()));

        // 3. Cancel Reservation (removes from database on confirmation)
        boolean deleted = dao.deleteByPNR(pnr);
        assertTrue("Cancellation removes booking from database", deleted);

        // 4. Verify no longer exists
        Reservation afterDelete = dao.findByPNR(pnr);
        assertTrue("Cancelled PNR can no longer be retrieved", afterDelete == null);
        assertTrue("isPNRExists returns false after cancellation", !dao.isPNRExists(pnr));
    }
}

