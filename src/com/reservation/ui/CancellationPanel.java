package com.reservation.ui;

import com.reservation.dao.ReservationDao;
import com.reservation.model.Reservation;
import com.reservation.model.User;
import com.reservation.util.ValidationUtil;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class CancellationPanel extends JPanel {

    private final User currentUser;
    private final ReservationDao reservationDao = new ReservationDao();
    private final Runnable onCancellationSuccess;

    private JTextField pnrInputField;
    private JButton fetchButton;
    private JButton confirmCancelButton;

    // Display Labels
    private JPanel detailsCard;
    private JLabel pnrValueLabel;
    private JLabel passengerValueLabel;
    private JLabel trainValueLabel;
    private JLabel classValueLabel;
    private JLabel dateValueLabel;
    private JLabel routeValueLabel;
    private JLabel statusValueLabel;
    private JLabel bookedByValueLabel;
    private JLabel bookedAtValueLabel;
    private JLabel statusNoticeLabel;

    private Reservation currentFetchedReservation = null;

    public CancellationPanel(User currentUser, Runnable onCancellationSuccess) {
        this.currentUser = currentUser;
        this.onCancellationSuccess = onCancellationSuccess;
        setLayout(new BorderLayout());
        setBackground(UIStyle.BG_LIGHT);
        setBorder(new EmptyBorder(20, 25, 20, 25));

        initUI();
    }

    private void initUI() {
        JPanel contentCard = new JPanel();
        contentCard.setLayout(new BoxLayout(contentCard, BoxLayout.Y_AXIS));
        contentCard.setBackground(UIStyle.CARD_BG);
        contentCard.setBorder(UIStyle.createCardBorder());

        // Header
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JLabel titleLabel = new JLabel("Ticket Cancellation Form");
        titleLabel.setFont(UIStyle.FONT_TITLE);
        titleLabel.setForeground(UIStyle.PRIMARY);

        JLabel subtitleLabel = new JLabel("Retrieve your booking details by PNR number and confirm cancellation.");
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

        // PNR Input + Fetch Section
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchBar.setOpaque(false);
        searchBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

        JLabel pnrPrompt = new JLabel("Enter PNR Number:");
        pnrPrompt.setFont(UIStyle.FONT_BOLD);
        pnrPrompt.setForeground(UIStyle.TEXT_MAIN);

        pnrInputField = UIStyle.createTextField();
        pnrInputField.setPreferredSize(new Dimension(220, 36));
        pnrInputField.setFont(UIStyle.FONT_MONO);

        fetchButton = UIStyle.createPrimaryButton("Fetch Details");
        fetchButton.addActionListener(e -> fetchBookingDetails());

        JButton clearButton = UIStyle.createSecondaryButton("Clear");
        clearButton.addActionListener(e -> resetForm());

        searchBar.add(pnrPrompt);
        searchBar.add(pnrInputField);
        searchBar.add(fetchButton);
        searchBar.add(clearButton);

        contentCard.add(searchBar);
        contentCard.add(Box.createRigidArea(new Dimension(0, 20)));

        // Booking Details Card (Visible after fetch)
        detailsCard = new JPanel();
        detailsCard.setLayout(new BoxLayout(detailsCard, BoxLayout.Y_AXIS));
        detailsCard.setBackground(new Color(248, 250, 252));
        detailsCard.setBorder(new CompoundBorder(
                new LineBorder(UIStyle.BORDER_COLOR, 1, true),
                new EmptyBorder(18, 20, 18, 20)
        ));
        detailsCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));
        detailsCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel detailsTitle = new JLabel("Booking Details");
        detailsTitle.setFont(UIStyle.FONT_HEADER);
        detailsTitle.setForeground(UIStyle.PRIMARY);
        detailsCard.add(detailsTitle);
        detailsCard.add(Box.createRigidArea(new Dimension(0, 12)));

        JPanel grid = new JPanel(new GridLayout(5, 4, 12, 10));
        grid.setOpaque(false);

        pnrValueLabel = createValueLabel("-");
        passengerValueLabel = createValueLabel("-");
        trainValueLabel = createValueLabel("-");
        classValueLabel = createValueLabel("-");
        dateValueLabel = createValueLabel("-");
        routeValueLabel = createValueLabel("-");
        statusValueLabel = createValueLabel("-");
        bookedByValueLabel = createValueLabel("-");
        bookedAtValueLabel = createValueLabel("-");

        grid.add(createFieldHeader("PNR Number:"));
        grid.add(pnrValueLabel);
        grid.add(createFieldHeader("Passenger Name:"));
        grid.add(passengerValueLabel);

        grid.add(createFieldHeader("Train Details:"));
        grid.add(trainValueLabel);
        grid.add(createFieldHeader("Class Type:"));
        grid.add(classValueLabel);

        grid.add(createFieldHeader("Journey Date:"));
        grid.add(dateValueLabel);
        grid.add(createFieldHeader("Route:"));
        grid.add(routeValueLabel);

        grid.add(createFieldHeader("Booking Status:"));
        grid.add(statusValueLabel);
        grid.add(createFieldHeader("Booked By:"));
        grid.add(bookedByValueLabel);

        grid.add(createFieldHeader("Booking Date:"));
        grid.add(bookedAtValueLabel);
        grid.add(new JLabel("")); // spacer
        grid.add(new JLabel("")); // spacer

        detailsCard.add(grid);
        detailsCard.add(Box.createRigidArea(new Dimension(0, 15)));

        statusNoticeLabel = new JLabel("Please enter a valid PNR number and click 'Fetch Details' to view reservation.");
        statusNoticeLabel.setFont(UIStyle.FONT_REGULAR);
        statusNoticeLabel.setForeground(UIStyle.TEXT_MUTED);
        detailsCard.add(statusNoticeLabel);

        contentCard.add(detailsCard);
        contentCard.add(Box.createRigidArea(new Dimension(0, 20)));

        // Action Buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setOpaque(false);
        actionPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

        confirmCancelButton = UIStyle.createDangerButton("Confirm Cancellation");
        confirmCancelButton.setEnabled(false);
        confirmCancelButton.addActionListener(e -> confirmCancellation());

        actionPanel.add(confirmCancelButton);
        contentCard.add(actionPanel);

        // Enter key in PNR field
        pnrInputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    fetchBookingDetails();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(contentCard);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JLabel createFieldHeader(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UIStyle.FONT_BOLD);
        l.setForeground(UIStyle.TEXT_MUTED);
        return l;
    }

    private JLabel createValueLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UIStyle.FONT_REGULAR);
        l.setForeground(UIStyle.TEXT_MAIN);
        return l;
    }

    public void setPNRAndFetch(String pnr) {
        pnrInputField.setText(pnr);
        fetchBookingDetails();
    }

    private void fetchBookingDetails() {
        String pnr = pnrInputField.getText().trim();
        if (ValidationUtil.isNullOrEmpty(pnr)) {
            JOptionPane.showMessageDialog(this, "Please enter a PNR number to fetch details.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            pnrInputField.requestFocus();
            return;
        }

        Reservation res = reservationDao.findByPNR(pnr);

        if (res == null) {
            resetDetails();
            statusNoticeLabel.setText("❌ No reservation found with PNR: " + pnr + ". Please verify and try again.");
            statusNoticeLabel.setForeground(UIStyle.DANGER);
            confirmCancelButton.setEnabled(false);
            currentFetchedReservation = null;

            JOptionPane.showMessageDialog(
                    this,
                    "No booking found for PNR: " + pnr + ".\nPlease double check the number and try again.",
                    "PNR Not Found",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        currentFetchedReservation = res;
        pnrValueLabel.setText(res.getPnr());
        passengerValueLabel.setText(res.getPassengerName());
        trainValueLabel.setText(res.getTrainNumber() + " - " + res.getTrainName());
        classValueLabel.setText(res.getClassType());
        dateValueLabel.setText(res.getJourneyDate());
        routeValueLabel.setText(res.getSource() + " ➔ " + res.getDestination());
        statusValueLabel.setText(res.getStatus());
        bookedByValueLabel.setText(res.getBookedBy());
        bookedAtValueLabel.setText(res.getBookingDate());

        statusNoticeLabel.setText("✅ Booking details retrieved successfully. Review the details above before confirming cancellation.");
        statusNoticeLabel.setForeground(UIStyle.SUCCESS);
        confirmCancelButton.setEnabled(true);
    }

    private void confirmCancellation() {
        if (currentFetchedReservation == null) {
            JOptionPane.showMessageDialog(this, "No active booking loaded to cancel.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String pnr = currentFetchedReservation.getPnr();
        String passenger = currentFetchedReservation.getPassengerName();
        String train = currentFetchedReservation.getTrainName();

        // "Are you sure?" confirmation dialog
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to cancel this booking?\n\n" +
                        "• PNR Number: " + pnr + "\n" +
                        "• Passenger: " + passenger + "\n" +
                        "• Train: " + train + "\n" +
                        "• Date: " + currentFetchedReservation.getJourneyDate() + "\n\n" +
                        "This will permanently remove the booking from the database.",
                "Confirm Cancellation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            // Remove the booking from database
            boolean deleted = reservationDao.deleteByPNR(pnr);

            if (deleted) {
                JOptionPane.showMessageDialog(
                        this,
                        "Booking cancellation confirmed!\n\nReservation with PNR " + pnr + " has been successfully removed from the database.",
                        "Cancellation Successful",
                        JOptionPane.INFORMATION_MESSAGE
                );

                resetForm();

                if (onCancellationSuccess != null) {
                    onCancellationSuccess.run();
                }
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Failed to remove reservation from database. Please try again.",
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    private void resetDetails() {
        pnrValueLabel.setText("-");
        passengerValueLabel.setText("-");
        trainValueLabel.setText("-");
        classValueLabel.setText("-");
        dateValueLabel.setText("-");
        routeValueLabel.setText("-");
        statusValueLabel.setText("-");
        bookedByValueLabel.setText("-");
        bookedAtValueLabel.setText("-");
    }

    private void resetForm() {
        pnrInputField.setText("");
        resetDetails();
        statusNoticeLabel.setText("Please enter a valid PNR number and click 'Fetch Details' to view reservation.");
        statusNoticeLabel.setForeground(UIStyle.TEXT_MUTED);
        confirmCancelButton.setEnabled(false);
        currentFetchedReservation = null;
    }
}

