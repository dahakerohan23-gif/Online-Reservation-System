package com.reservation.ui;

import com.reservation.dao.ReservationDao;
import com.reservation.dao.TrainDao;
import com.reservation.model.Reservation;
import com.reservation.model.Train;
import com.reservation.model.User;
import com.reservation.util.PNRGenerator;
import com.reservation.util.ValidationUtil;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReservationPanel extends JPanel {

    private final User currentUser;
    private final TrainDao trainDao = new TrainDao();
    private final ReservationDao reservationDao = new ReservationDao();
    private final Runnable onBookingSuccess;

    private JTextField passengerNameField;
    private JTextField trainNumberField;
    private JTextField trainNameField;
    private JComboBox<String> classTypeCombo;
    private JTextField journeyDateField;
    private JTextField sourceField;
    private JTextField destinationField;
    private JLabel trainLookupStatus;

    public ReservationPanel(User currentUser, Runnable onBookingSuccess) {
        this.currentUser = currentUser;
        this.onBookingSuccess = onBookingSuccess;
        setLayout(new BorderLayout());
        setBackground(UIStyle.BG_LIGHT);
        setBorder(new EmptyBorder(20, 25, 20, 25));

        initUI();
    }

    private void initUI() {
        // Main scrollable container
        JPanel contentCard = new JPanel();
        contentCard.setLayout(new BoxLayout(contentCard, BoxLayout.Y_AXIS));
        contentCard.setBackground(UIStyle.CARD_BG);
        contentCard.setBorder(UIStyle.createCardBorder());

        // Header
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JLabel titleLabel = new JLabel("Book Ticket / New Reservation");
        titleLabel.setFont(UIStyle.FONT_TITLE);
        titleLabel.setForeground(UIStyle.PRIMARY);

        JLabel subtitleLabel = new JLabel("Fill in the passenger and journey details below to reserve your ticket.");
        subtitleLabel.setFont(UIStyle.FONT_REGULAR);
        subtitleLabel.setForeground(UIStyle.TEXT_MUTED);

        JPanel headerText = new JPanel();
        headerText.setLayout(new BoxLayout(headerText, BoxLayout.Y_AXIS));
        headerText.setOpaque(false);
        headerText.add(titleLabel);
        headerText.add(Box.createRigidArea(new Dimension(0, 4)));
        headerText.add(subtitleLabel);

        titlePanel.add(headerText, BorderLayout.WEST);

        contentCard.add(titlePanel);
        contentCard.add(Box.createRigidArea(new Dimension(0, 18)));
        contentCard.add(new JSeparator());
        contentCard.add(Box.createRigidArea(new Dimension(0, 18)));

        // Form Grid
        JPanel formGrid = new JPanel(new GridBagLayout());
        formGrid.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);

        // 1. Passenger Name
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.3;
        formGrid.add(createFieldLabel("Passenger Name *"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        passengerNameField = UIStyle.createTextField();
        passengerNameField.setPreferredSize(new Dimension(280, 36));
        formGrid.add(passengerNameField, gbc);

        // 2. Train Number & Lookup
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.3;
        formGrid.add(createFieldLabel("Train Number *"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        JPanel trainNumPanel = new JPanel(new BorderLayout(8, 0));
        trainNumPanel.setOpaque(false);

        trainNumberField = UIStyle.createTextField();
        trainNumberField.setPreferredSize(new Dimension(160, 36));

        JButton selectTrainBtn = UIStyle.createSecondaryButton("Select Train...");
        selectTrainBtn.addActionListener(e -> showTrainPicker());

        trainNumPanel.add(trainNumberField, BorderLayout.CENTER);
        trainNumPanel.add(selectTrainBtn, BorderLayout.EAST);
        formGrid.add(trainNumPanel, gbc);

        // Train Lookup Feedback Label
        gbc.gridx = 1;
        gbc.gridy = 2;
        trainLookupStatus = new JLabel("Enter train number (e.g. 12001, 12951, 22436)");
        trainLookupStatus.setFont(UIStyle.FONT_SMALL);
        trainLookupStatus.setForeground(UIStyle.TEXT_MUTED);
        formGrid.add(trainLookupStatus, gbc);

        // 3. Train Name (Auto-populated from Train Number)
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.3;
        formGrid.add(createFieldLabel("Train Name (Auto-populated) *"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        trainNameField = UIStyle.createTextField();
        trainNameField.setPreferredSize(new Dimension(280, 36));
        trainNameField.setEditable(false);
        trainNameField.setBackground(new Color(248, 250, 252));
        formGrid.add(trainNameField, gbc);

        // 4. Class Type
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0.3;
        formGrid.add(createFieldLabel("Class Type *"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        String[] classes = {
                "1A - AC First Class",
                "2A - AC 2-Tier",
                "3A - AC 3-Tier",
                "SL - Sleeper Class",
                "CC - AC Chair Car",
                "EC - Executive Chair Car",
                "2S - Second Sitting"
        };
        classTypeCombo = new JComboBox<>(classes);
        classTypeCombo.setFont(UIStyle.FONT_REGULAR);
        classTypeCombo.setPreferredSize(new Dimension(280, 36));
        classTypeCombo.setBackground(Color.WHITE);
        formGrid.add(classTypeCombo, gbc);

        // 5. Date of Journey (YYYY-MM-DD)
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.weightx = 0.3;
        formGrid.add(createFieldLabel("Date of Journey (YYYY-MM-DD) *"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        JPanel datePanel = new JPanel(new BorderLayout(8, 0));
        datePanel.setOpaque(false);

        LocalDate tomorrow = LocalDate.now().plusDays(1);
        journeyDateField = UIStyle.createTextField();
        journeyDateField.setText(tomorrow.format(ValidationUtil.DATE_FORMATTER));
        journeyDateField.setPreferredSize(new Dimension(160, 36));

        JPanel quickDatePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        quickDatePanel.setOpaque(false);
        JButton todayBtn = UIStyle.createSecondaryButton("Today");
        todayBtn.setFont(UIStyle.FONT_SMALL);
        todayBtn.addActionListener(e -> journeyDateField.setText(LocalDate.now().format(ValidationUtil.DATE_FORMATTER)));

        JButton tmrwBtn = UIStyle.createSecondaryButton("+1 Day");
        tmrwBtn.setFont(UIStyle.FONT_SMALL);
        tmrwBtn.addActionListener(e -> journeyDateField.setText(LocalDate.now().plusDays(1).format(ValidationUtil.DATE_FORMATTER)));

        JButton weekBtn = UIStyle.createSecondaryButton("+7 Days");
        weekBtn.setFont(UIStyle.FONT_SMALL);
        weekBtn.addActionListener(e -> journeyDateField.setText(LocalDate.now().plusWeeks(1).format(ValidationUtil.DATE_FORMATTER)));

        quickDatePanel.add(todayBtn);
        quickDatePanel.add(tmrwBtn);
        quickDatePanel.add(weekBtn);

        datePanel.add(journeyDateField, BorderLayout.CENTER);
        datePanel.add(quickDatePanel, BorderLayout.EAST);
        formGrid.add(datePanel, gbc);

        // 6. Source Station
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.weightx = 0.3;
        formGrid.add(createFieldLabel("Source Station *"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        sourceField = UIStyle.createTextField();
        sourceField.setPreferredSize(new Dimension(280, 36));
        formGrid.add(sourceField, gbc);

        // 7. Destination Station
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.weightx = 0.3;
        formGrid.add(createFieldLabel("Destination Station *"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        destinationField = UIStyle.createTextField();
        destinationField.setPreferredSize(new Dimension(280, 36));
        formGrid.add(destinationField, gbc);

        contentCard.add(formGrid);
        contentCard.add(Box.createRigidArea(new Dimension(0, 20)));

        // Action Buttons Panel
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actionPanel.setOpaque(false);
        actionPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JButton clearBtn = UIStyle.createSecondaryButton("Reset Form");
        clearBtn.addActionListener(e -> resetForm());

        JButton bookBtn = UIStyle.createPrimaryButton("Book Ticket (Generate PNR)");
        bookBtn.addActionListener(e -> performBooking());

        actionPanel.add(clearBtn);
        actionPanel.add(bookBtn);

        contentCard.add(actionPanel);

        // Auto-lookup train name dynamically as user types
        trainNumberField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                lookupTrain();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                lookupTrain();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                lookupTrain();
            }
        });

        JScrollPane scrollPane = new JScrollPane(contentCard);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UIStyle.FONT_BOLD);
        label.setForeground(UIStyle.TEXT_MAIN);
        return label;
    }

    private void lookupTrain() {
        String numText = trainNumberField.getText().trim();
        if (numText.isEmpty()) {
            trainNameField.setText("");
            trainLookupStatus.setText("Enter train number (e.g. 12001, 12951, 22436)");
            trainLookupStatus.setForeground(UIStyle.TEXT_MUTED);
            return;
        }

        if (!ValidationUtil.isPositiveInteger(numText)) {
            trainNameField.setText("");
            trainLookupStatus.setText("Invalid format: train number must be numeric.");
            trainLookupStatus.setForeground(UIStyle.DANGER);
            return;
        }

        int trainNo = Integer.parseInt(numText);
        Train train = trainDao.findByNumber(trainNo);

        if (train != null) {
            trainNameField.setText(train.getTrainName());
            trainLookupStatus.setText("Found: " + train.getTrainName() + " (" + train.getSource() + " -> " + train.getDestination() + ")");
            trainLookupStatus.setForeground(UIStyle.SUCCESS);

            // Auto-fill source and destination if blank
            if (sourceField.getText().trim().isEmpty()) {
                sourceField.setText(train.getSource());
            }
            if (destinationField.getText().trim().isEmpty()) {
                destinationField.setText(train.getDestination());
            }
        } else {
            trainNameField.setText("");
            trainLookupStatus.setText("No train found with number " + trainNo + ". You can browse the Train Directory.");
            trainLookupStatus.setForeground(UIStyle.DANGER);
        }
    }

    public void setTrain(Train train) {
        if (train != null) {
            trainNumberField.setText(String.valueOf(train.getTrainNumber()));
            trainNameField.setText(train.getTrainName());
            sourceField.setText(train.getSource());
            destinationField.setText(train.getDestination());
            trainLookupStatus.setText("Selected: " + train.getTrainName());
            trainLookupStatus.setForeground(UIStyle.SUCCESS);
        }
    }

    private void showTrainPicker() {
        List<Train> trains = trainDao.getAllTrains();
        if (trains.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No trains available in directory.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Train selected = (Train) JOptionPane.showInputDialog(
                this,
                "Select a train from the schedule:",
                "Train Directory Picker",
                JOptionPane.PLAIN_MESSAGE,
                null,
                trains.toArray(),
                trains.get(0)
        );

        if (selected != null) {
            setTrain(selected);
        }
    }

    private void performBooking() {
        String passengerName = passengerNameField.getText().trim();
        String trainNumStr = trainNumberField.getText().trim();
        String trainName = trainNameField.getText().trim();
        String classType = (String) classTypeCombo.getSelectedItem();
        String journeyDate = journeyDateField.getText().trim();
        String source = sourceField.getText().trim();
        String destination = destinationField.getText().trim();

        // 1. Validation checks
        if (ValidationUtil.isNullOrEmpty(passengerName)) {
            showValidationError("Passenger Name is required.");
            passengerNameField.requestFocus();
            return;
        }

        if (passengerName.length() < 2) {
            showValidationError("Passenger Name must be at least 2 characters long.");
            passengerNameField.requestFocus();
            return;
        }

        if (ValidationUtil.isNullOrEmpty(trainNumStr) || !ValidationUtil.isPositiveInteger(trainNumStr)) {
            showValidationError("Please enter a valid numeric Train Number.");
            trainNumberField.requestFocus();
            return;
        }

        if (ValidationUtil.isNullOrEmpty(trainName)) {
            showValidationError("Train Name cannot be empty. Please verify train number.");
            trainNumberField.requestFocus();
            return;
        }

        if (ValidationUtil.isNullOrEmpty(journeyDate) || !ValidationUtil.isValidJourneyDate(journeyDate)) {
            showValidationError("Invalid Date of Journey. Please use YYYY-MM-DD format for today or a future date.");
            journeyDateField.requestFocus();
            return;
        }

        if (ValidationUtil.isNullOrEmpty(source)) {
            showValidationError("Source Station is required.");
            sourceField.requestFocus();
            return;
        }

        if (ValidationUtil.isNullOrEmpty(destination)) {
            showValidationError("Destination Station is required.");
            destinationField.requestFocus();
            return;
        }

        if (source.equalsIgnoreCase(destination)) {
            showValidationError("Source and Destination stations cannot be identical.");
            destinationField.requestFocus();
            return;
        }

        // 2. Generate unique PNR
        String pnr;
        do {
            pnr = PNRGenerator.generatePNR();
        } while (reservationDao.isPNRExists(pnr));

        // 3. Format booking timestamp
        String bookingTimestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // 4. Create Reservation object
        Reservation reservation = new Reservation(
                pnr,
                passengerName,
                Integer.parseInt(trainNumStr),
                trainName,
                classType,
                journeyDate,
                source,
                destination,
                bookingTimestamp,
                "CONFIRMED",
                currentUser != null ? currentUser.getUsername() : "guest"
        );

        // 5. Save to database
        boolean success = reservationDao.createReservation(reservation);

        if (success) {
            // 6. Show Confirmation Dialog with full booking details
            showConfirmationDialog(reservation);

            // Reset form
            resetForm();

            // Refresh parent view if listener provided
            if (onBookingSuccess != null) {
                onBookingSuccess.run();
            }
        } else {
            JOptionPane.showMessageDialog(
                    this,
                    "Failed to save reservation to database. Please check connection and try again.",
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void showConfirmationDialog(Reservation r) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Booking Confirmation", true);
        dialog.setSize(480, 520);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(UIStyle.CARD_BG);
        panel.setBorder(new EmptyBorder(25, 30, 25, 30));

        // Success Header Badge
        JPanel badgePanel = new JPanel();
        badgePanel.setLayout(new BoxLayout(badgePanel, BoxLayout.Y_AXIS));
        badgePanel.setBackground(new Color(240, 253, 244));
        badgePanel.setBorder(new CompoundBorder(
                new LineBorder(new Color(187, 247, 208), 1, true),
                new EmptyBorder(12, 16, 12, 16)
        ));
        badgePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        badgePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 75));

        JLabel checkIcon = new JLabel("✅ Booking Confirmed!");
        checkIcon.setFont(UIStyle.FONT_HEADER);
        checkIcon.setForeground(UIStyle.SUCCESS);
        checkIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel pnrBanner = new JLabel("PNR NUMBER: " + r.getPnr());
        pnrBanner.setFont(new Font("Segoe UI", Font.BOLD, 18));
        pnrBanner.setForeground(UIStyle.PRIMARY);
        pnrBanner.setAlignmentX(Component.CENTER_ALIGNMENT);

        badgePanel.add(checkIcon);
        badgePanel.add(Box.createRigidArea(new Dimension(0, 4)));
        badgePanel.add(pnrBanner);

        panel.add(badgePanel);
        panel.add(Box.createRigidArea(new Dimension(0, 18)));

        // Details Grid
        JPanel detailsGrid = new JPanel(new GridLayout(7, 2, 8, 8));
        detailsGrid.setOpaque(false);
        detailsGrid.setAlignmentX(Component.CENTER_ALIGNMENT);

        addDetailRow(detailsGrid, "Passenger Name:", r.getPassengerName());
        addDetailRow(detailsGrid, "Train Number & Name:", r.getTrainNumber() + " - " + r.getTrainName());
        addDetailRow(detailsGrid, "Class:", r.getClassType());
        addDetailRow(detailsGrid, "Date of Journey:", r.getJourneyDate());
        addDetailRow(detailsGrid, "Route:", r.getSource() + " ➔ " + r.getDestination());
        addDetailRow(detailsGrid, "Booking Status:", r.getStatus());
        addDetailRow(detailsGrid, "Booked At:", r.getBookingDate());

        panel.add(detailsGrid);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Notice Note
        JLabel notice = new JLabel("<html><i>Please save your 10-digit PNR number for cancellation or status inquiries.</i></html>");
        notice.setFont(UIStyle.FONT_SMALL);
        notice.setForeground(UIStyle.TEXT_MUTED);
        notice.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(notice);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Close Button
        JButton okBtn = UIStyle.createPrimaryButton("Done / Print Receipt");
        okBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        okBtn.addActionListener(e -> dialog.dispose());
        panel.add(okBtn);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    private void addDetailRow(JPanel panel, String label, String value) {
        JLabel l = new JLabel(label);
        l.setFont(UIStyle.FONT_BOLD);
        l.setForeground(UIStyle.TEXT_MUTED);

        JLabel v = new JLabel(value);
        v.setFont(UIStyle.FONT_REGULAR);
        v.setForeground(UIStyle.TEXT_MAIN);

        panel.add(l);
        panel.add(v);
    }

    private void showValidationError(String msg) {
        JOptionPane.showMessageDialog(
                this,
                msg,
                "Input Validation Error",
                JOptionPane.WARNING_MESSAGE
        );
    }

    private void resetForm() {
        passengerNameField.setText("");
        trainNumberField.setText("");
        trainNameField.setText("");
        sourceField.setText("");
        destinationField.setText("");
        journeyDateField.setText(LocalDate.now().plusDays(1).format(ValidationUtil.DATE_FORMATTER));
        classTypeCombo.setSelectedIndex(0);
        trainLookupStatus.setText("Enter train number (e.g. 12001, 12951, 22436)");
        trainLookupStatus.setForeground(UIStyle.TEXT_MUTED);
    }
}

