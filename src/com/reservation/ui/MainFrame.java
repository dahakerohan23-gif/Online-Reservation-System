package com.reservation.ui;

import com.reservation.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MainFrame extends JFrame {

    private final User currentUser;

    private JTabbedPane tabbedPane;
    private ReservationPanel reservationPanel;
    private CancellationPanel cancellationPanel;
    private ViewBookingsPanel viewBookingsPanel;
    private TrainDirectoryPanel trainDirectoryPanel;

    public MainFrame(User currentUser) {
        this.currentUser = currentUser;
        setTitle("Online Reservation System - Railway Management Portal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1080, 720);
        setMinimumSize(new Dimension(880, 600));
        setLocationRelativeTo(null);

        initUI();
    }

    private void initUI() {
        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(UIStyle.BG_LIGHT);

        // Header Banner
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UIStyle.PRIMARY);
        headerPanel.setBorder(new EmptyBorder(16, 24, 16, 24));

        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        brandPanel.setOpaque(false);

        JLabel brandIcon = new JLabel("🚆");
        brandIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));

        JPanel brandText = new JPanel();
        brandText.setLayout(new BoxLayout(brandText, BoxLayout.Y_AXIS));
        brandText.setOpaque(false);

        JLabel brandTitle = new JLabel("Online Reservation System");
        brandTitle.setFont(UIStyle.FONT_TITLE);
        brandTitle.setForeground(Color.WHITE);

        JLabel brandSub = new JLabel("Indian Railways Ticket Reservation & PNR Management Portal");
        brandSub.setFont(UIStyle.FONT_SMALL);
        brandSub.setForeground(new Color(224, 231, 255));

        brandText.add(brandTitle);
        brandText.add(brandSub);

        brandPanel.add(brandIcon);
        brandPanel.add(brandText);

        // User info + Logout button
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        userPanel.setOpaque(false);

        String userDisplay = (currentUser != null) ? currentUser.getFullName() + " (" + currentUser.getUsername() + ")" : "Guest User";
        JLabel userLabel = new JLabel("👤 " + userDisplay);
        userLabel.setFont(UIStyle.FONT_BOLD);
        userLabel.setForeground(Color.WHITE);

        JButton logoutBtn = UIStyle.createSecondaryButton("Log Out");
        logoutBtn.addActionListener(e -> logout());

        userPanel.add(userLabel);
        userPanel.add(logoutBtn);

        headerPanel.add(brandPanel, BorderLayout.WEST);
        headerPanel.add(userPanel, BorderLayout.EAST);

        rootPanel.add(headerPanel, BorderLayout.NORTH);

        // Tabbed Pane Setup
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UIStyle.FONT_SUBHEADER);
        tabbedPane.setBackground(Color.WHITE);

        // Initialize panels with reactive callbacks
        reservationPanel = new ReservationPanel(currentUser, () -> {
            if (viewBookingsPanel != null) {
                viewBookingsPanel.refreshData();
            }
        });

        cancellationPanel = new CancellationPanel(currentUser, () -> {
            if (viewBookingsPanel != null) {
                viewBookingsPanel.refreshData();
            }
        });

        viewBookingsPanel = new ViewBookingsPanel(currentUser, pnr -> {
            // Switch to Cancellation tab and populate PNR
            tabbedPane.setSelectedComponent(cancellationPanel);
            cancellationPanel.setPNRAndFetch(pnr);
        });

        trainDirectoryPanel = new TrainDirectoryPanel(train -> {
            // Switch to Reservation tab and populate train
            tabbedPane.setSelectedComponent(reservationPanel);
            reservationPanel.setTrain(train);
        });

        tabbedPane.addTab("  🎫 Reservation Form  ", reservationPanel);
        tabbedPane.addTab("  ❌ Cancellation Form  ", cancellationPanel);
        tabbedPane.addTab("  📋 My Bookings / All Records  ", viewBookingsPanel);
        tabbedPane.addTab("  🚆 Train Schedule Directory  ", trainDirectoryPanel);

        rootPanel.add(tabbedPane, BorderLayout.CENTER);

        // Bottom Status Bar
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(new Color(241, 245, 249));
        statusBar.setBorder(new EmptyBorder(6, 16, 6, 16));

        JLabel dbStatus = new JLabel("🟢 Database: SQLite (reservation.db) Connected | Security: PreparedStatement Enabled");
        dbStatus.setFont(UIStyle.FONT_SMALL);
        dbStatus.setForeground(UIStyle.TEXT_MUTED);

        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        JLabel timeStatus = new JLabel("System Ready · " + now);
        timeStatus.setFont(UIStyle.FONT_SMALL);
        timeStatus.setForeground(UIStyle.TEXT_MUTED);

        statusBar.add(dbStatus, BorderLayout.WEST);
        statusBar.add(timeStatus, BorderLayout.EAST);

        rootPanel.add(statusBar, BorderLayout.SOUTH);

        setContentPane(rootPanel);
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to log out?",
                "Log Out",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            SwingUtilities.invokeLater(() -> {
                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setVisible(true);
            });
        }
    }
}

