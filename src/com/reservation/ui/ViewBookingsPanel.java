package com.reservation.ui;

import com.reservation.dao.ReservationDao;
import com.reservation.model.Reservation;
import com.reservation.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

public class ViewBookingsPanel extends JPanel {

    private final User currentUser;
    private final ReservationDao reservationDao = new ReservationDao();
    private final Consumer<String> onDirectCancelRequested;

    private JTable bookingsTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField searchField;
    private JLabel countLabel;

    public ViewBookingsPanel(User currentUser, Consumer<String> onDirectCancelRequested) {
        this.currentUser = currentUser;
        this.onDirectCancelRequested = onDirectCancelRequested;
        setLayout(new BorderLayout());
        setBackground(UIStyle.BG_LIGHT);
        setBorder(new EmptyBorder(20, 25, 20, 25));

        initUI();
        refreshData();
    }

    private void initUI() {
        JPanel contentCard = new JPanel(new BorderLayout(0, 15));
        contentCard.setBackground(UIStyle.CARD_BG);
        contentCard.setBorder(UIStyle.createCardBorder());

        // Header + Toolbar
        JPanel topPanel = new JPanel(new BorderLayout(15, 10));
        topPanel.setOpaque(false);

        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setOpaque(false);

        JLabel titleLabel = new JLabel("All Reservations & Ticket Bookings");
        titleLabel.setFont(UIStyle.FONT_TITLE);
        titleLabel.setForeground(UIStyle.PRIMARY);

        countLabel = new JLabel("Total Bookings: 0");
        countLabel.setFont(UIStyle.FONT_REGULAR);
        countLabel.setForeground(UIStyle.TEXT_MUTED);

        titleBox.add(titleLabel);
        titleBox.add(Box.createRigidArea(new Dimension(0, 4)));
        titleBox.add(countLabel);

        // Search & Action Toolbar
        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        toolBar.setOpaque(false);

        JLabel searchLabel = new JLabel("Filter / Search:");
        searchLabel.setFont(UIStyle.FONT_BOLD);
        searchLabel.setForeground(UIStyle.TEXT_MAIN);

        searchField = UIStyle.createTextField();
        searchField.setPreferredSize(new Dimension(200, 34));
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
        });

        JButton refreshBtn = UIStyle.createSecondaryButton("Refresh");
        refreshBtn.addActionListener(e -> refreshData());

        JButton viewDetailsBtn = UIStyle.createSecondaryButton("View Ticket Details");
        viewDetailsBtn.addActionListener(e -> viewSelectedTicket());

        JButton cancelSelectedBtn = UIStyle.createDangerButton("Cancel Selected");
        cancelSelectedBtn.addActionListener(e -> cancelSelectedTicket());

        toolBar.add(searchLabel);
        toolBar.add(searchField);
        toolBar.add(refreshBtn);
        toolBar.add(viewDetailsBtn);
        toolBar.add(cancelSelectedBtn);

        topPanel.add(titleBox, BorderLayout.WEST);
        topPanel.add(toolBar, BorderLayout.EAST);

        contentCard.add(topPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {
                "PNR Number", "Passenger Name", "Train No.", "Train Name",
                "Class", "Journey Date", "Source", "Destination", "Booking Time", "Status", "Booked By"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        bookingsTable = new JTable(tableModel);
        bookingsTable.setFont(UIStyle.FONT_REGULAR);
        bookingsTable.setRowHeight(32);
        bookingsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bookingsTable.getTableHeader().setFont(UIStyle.FONT_BOLD);
        bookingsTable.getTableHeader().setBackground(new Color(241, 245, 249));
        bookingsTable.getTableHeader().setForeground(UIStyle.TEXT_MAIN);
        bookingsTable.setGridColor(UIStyle.BORDER_COLOR);
        bookingsTable.setShowGrid(true);

        // Center align PNR and date
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        bookingsTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        bookingsTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        bookingsTable.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
        bookingsTable.getColumnModel().getColumn(9).setCellRenderer(centerRenderer);

        sorter = new TableRowSorter<>(tableModel);
        bookingsTable.setRowSorter(sorter);

        JScrollPane scrollPane = new JScrollPane(bookingsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UIStyle.BORDER_COLOR));

        contentCard.add(scrollPane, BorderLayout.CENTER);

        add(contentCard, BorderLayout.CENTER);
    }

    private void applyFilter() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + query));
        }
    }

    public void refreshData() {
        tableModel.setRowCount(0);
        List<Reservation> list;
        if (currentUser != null && currentUser.isAdmin()) {
            list = reservationDao.getAllReservations();
        } else if (currentUser != null) {
            list = reservationDao.getReservationsByUser(currentUser.getUsername());
        } else {
            list = reservationDao.getAllReservations();
        }

        for (Reservation r : list) {
            tableModel.addRow(new Object[]{
                    r.getPnr(),
                    r.getPassengerName(),
                    r.getTrainNumber(),
                    r.getTrainName(),
                    r.getClassType(),
                    r.getJourneyDate(),
                    r.getSource(),
                    r.getDestination(),
                    r.getBookingDate(),
                    r.getStatus(),
                    r.getBookedBy()
            });
        }

        countLabel.setText("Total Bookings: " + list.size() + (currentUser != null && currentUser.isAdmin() ? " (All Users - Admin Mode)" : " (Logged in as: " + (currentUser != null ? currentUser.getUsername() : "Guest") + ")"));
    }

    private void viewSelectedTicket() {
        int selectedRow = bookingsTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a booking from the table to view details.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int modelRow = bookingsTable.convertRowIndexToModel(selectedRow);
        String pnr = (String) tableModel.getValueAt(modelRow, 0);
        Reservation res = reservationDao.findByPNR(pnr);
        if (res == null) {
            JOptionPane.showMessageDialog(this, "Booking not found in database.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String message = String.format(
                "=========================================\n" +
                "         PASSENGER TICKET RECEIPT        \n" +
                "=========================================\n" +
                "PNR Number       : %s\n" +
                "Passenger Name   : %s\n" +
                "Train Number     : %d\n" +
                "Train Name       : %s\n" +
                "Class Type       : %s\n" +
                "Journey Date     : %s\n" +
                "Source Station   : %s\n" +
                "Destination      : %s\n" +
                "Booking Time     : %s\n" +
                "Status           : %s\n" +
                "Booked By        : %s\n" +
                "=========================================",
                res.getPnr(), res.getPassengerName(), res.getTrainNumber(),
                res.getTrainName(), res.getClassType(), res.getJourneyDate(),
                res.getSource(), res.getDestination(), res.getBookingDate(),
                res.getStatus(), res.getBookedBy()
        );

        JTextArea ta = new JTextArea(message);
        ta.setFont(new Font("Consolas", Font.PLAIN, 13));
        ta.setEditable(false);
        ta.setBackground(new Color(248, 250, 252));
        ta.setBorder(new EmptyBorder(10, 10, 10, 10));

        JOptionPane.showMessageDialog(this, new JScrollPane(ta), "Ticket Details - PNR " + res.getPnr(), JOptionPane.INFORMATION_MESSAGE);
    }

    private void cancelSelectedTicket() {
        int selectedRow = bookingsTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a booking from the table to cancel.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int modelRow = bookingsTable.convertRowIndexToModel(selectedRow);
        String pnr = (String) tableModel.getValueAt(modelRow, 0);

        if (onDirectCancelRequested != null) {
            onDirectCancelRequested.accept(pnr);
        }
    }
}

