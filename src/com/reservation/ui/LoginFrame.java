package com.reservation.ui;

import com.reservation.dao.UserDao;
import com.reservation.model.User;
import com.reservation.util.ValidationUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class LoginFrame extends JFrame {

    private final UserDao userDao = new UserDao();

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel statusLabel;

    public LoginFrame() {
        setTitle("Online Reservation System - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(460, 560);
        setLocationRelativeTo(null);
        setResizable(false);

        initUI();
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(UIStyle.BG_LIGHT);

        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(UIStyle.PRIMARY);
        headerPanel.setBorder(new EmptyBorder(30, 25, 25, 25));

        JLabel iconLabel = new JLabel("🚆");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel("Online Reservation System");
        titleLabel.setFont(UIStyle.FONT_TITLE);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Train & Transport Ticket Booking Portal");
        subtitleLabel.setFont(UIStyle.FONT_REGULAR);
        subtitleLabel.setForeground(new Color(224, 231, 255));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(iconLabel);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        headerPanel.add(titleLabel);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        headerPanel.add(subtitleLabel);

        // Form Card Panel
        JPanel formCard = new JPanel();
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));
        formCard.setBackground(UIStyle.CARD_BG);
        formCard.setBorder(new EmptyBorder(25, 30, 25, 30));

        JLabel loginHeader = new JLabel("Sign In to Your Account");
        loginHeader.setFont(UIStyle.FONT_HEADER);
        loginHeader.setForeground(UIStyle.TEXT_MAIN);
        loginHeader.setAlignmentX(Component.LEFT_ALIGNMENT);

        formCard.add(loginHeader);
        formCard.add(Box.createRigidArea(new Dimension(0, 18)));

        // Username
        JLabel userLabel = new JLabel("Username");
        userLabel.setFont(UIStyle.FONT_BOLD);
        userLabel.setForeground(UIStyle.TEXT_MAIN);
        userLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        usernameField = UIStyle.createTextField();
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        usernameField.setAlignmentX(Component.LEFT_ALIGNMENT);

        formCard.add(userLabel);
        formCard.add(Box.createRigidArea(new Dimension(0, 5)));
        formCard.add(usernameField);
        formCard.add(Box.createRigidArea(new Dimension(0, 15)));

        // Password
        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(UIStyle.FONT_BOLD);
        passLabel.setForeground(UIStyle.TEXT_MAIN);
        passLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        passwordField = UIStyle.createPasswordField();
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);

        formCard.add(passLabel);
        formCard.add(Box.createRigidArea(new Dimension(0, 5)));
        formCard.add(passwordField);
        formCard.add(Box.createRigidArea(new Dimension(0, 10)));

        // Status / Error message
        statusLabel = new JLabel(" ");
        statusLabel.setFont(UIStyle.FONT_SMALL);
        statusLabel.setForeground(UIStyle.DANGER);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formCard.add(statusLabel);
        formCard.add(Box.createRigidArea(new Dimension(0, 12)));

        // Buttons
        JButton loginBtn = UIStyle.createPrimaryButton("Login");
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        loginBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginBtn.addActionListener(e -> performLogin());

        JButton registerBtn = UIStyle.createSecondaryButton("Create New Account");
        registerBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        registerBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        registerBtn.addActionListener(e -> showRegisterDialog());

        formCard.add(loginBtn);
        formCard.add(Box.createRigidArea(new Dimension(0, 10)));
        formCard.add(registerBtn);
        formCard.add(Box.createRigidArea(new Dimension(0, 15)));

        // Demo credentials hint
        JPanel demoBox = new JPanel();
        demoBox.setLayout(new BoxLayout(demoBox, BoxLayout.Y_AXIS));
        demoBox.setBackground(new Color(241, 245, 249));
        demoBox.setBorder(new EmptyBorder(8, 12, 8, 12));
        demoBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        demoBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));

        JLabel demoTitle = new JLabel("Default Demo Credentials:");
        demoTitle.setFont(UIStyle.FONT_BOLD);
        demoTitle.setForeground(UIStyle.TEXT_MUTED);

        JLabel demoDetails = new JLabel("admin / admin123  or  rohan / pass123");
        demoDetails.setFont(UIStyle.FONT_SMALL);
        demoDetails.setForeground(UIStyle.TEXT_MUTED);

        demoBox.add(demoTitle);
        demoBox.add(demoDetails);
        formCard.add(demoBox);

        // Enter key to submit
        KeyAdapter enterKeyAdapter = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    performLogin();
                }
            }
        };
        usernameField.addKeyListener(enterKeyAdapter);
        passwordField.addKeyListener(enterKeyAdapter);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(formCard, BorderLayout.CENTER);

        setContentPane(mainPanel);
    }

    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (ValidationUtil.isNullOrEmpty(username)) {
            showError("Please enter your username.");
            usernameField.requestFocus();
            return;
        }

        if (ValidationUtil.isNullOrEmpty(password)) {
            showError("Please enter your password.");
            passwordField.requestFocus();
            return;
        }

        statusLabel.setText("Authenticating...");
        statusLabel.setForeground(UIStyle.PRIMARY);

        User user = userDao.authenticate(username, password);

        if (user != null) {
            statusLabel.setText("Login successful!");
            statusLabel.setForeground(UIStyle.SUCCESS);

            SwingUtilities.invokeLater(() -> {
                dispose();
                MainFrame mainFrame = new MainFrame(user);
                mainFrame.setVisible(true);
            });
        } else {
            showError("Access Denied: Invalid username or password.");
            JOptionPane.showMessageDialog(
                    this,
                    "Access Denied!\nInvalid username or password. Please verify your credentials and try again.",
                    "Authentication Failed",
                    JOptionPane.ERROR_MESSAGE
            );
            passwordField.setText("");
            passwordField.requestFocus();
        }
    }

    private void showError(String msg) {
        statusLabel.setText(msg);
        statusLabel.setForeground(UIStyle.DANGER);
    }

    private void showRegisterDialog() {
        JDialog dialog = new JDialog(this, "Register New User", true);
        dialog.setSize(380, 420);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(UIStyle.CARD_BG);
        panel.setBorder(new EmptyBorder(20, 25, 20, 25));

        JLabel title = new JLabel("Create Account");
        title.setFont(UIStyle.FONT_HEADER);
        title.setForeground(UIStyle.PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField nameField = UIStyle.createTextField();
        JTextField newUsernameField = UIStyle.createTextField();
        JPasswordField newPasswordField = UIStyle.createPasswordField();
        JPasswordField confirmPasswordField = UIStyle.createPasswordField();

        panel.add(title);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        panel.add(new JLabel("Full Name:"));
        panel.add(Box.createRigidArea(new Dimension(0, 4)));
        panel.add(nameField);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        panel.add(new JLabel("Username:"));
        panel.add(Box.createRigidArea(new Dimension(0, 4)));
        panel.add(newUsernameField);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        panel.add(new JLabel("Password:"));
        panel.add(Box.createRigidArea(new Dimension(0, 4)));
        panel.add(newPasswordField);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        panel.add(new JLabel("Confirm Password:"));
        panel.add(Box.createRigidArea(new Dimension(0, 4)));
        panel.add(confirmPasswordField);
        panel.add(Box.createRigidArea(new Dimension(0, 18)));

        JButton registerSubmit = UIStyle.createPrimaryButton("Register");
        registerSubmit.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        registerSubmit.setAlignmentX(Component.LEFT_ALIGNMENT);

        registerSubmit.addActionListener(e -> {
            String name = nameField.getText().trim();
            String uName = newUsernameField.getText().trim();
            String pWord = new String(newPasswordField.getPassword());
            String cPWord = new String(confirmPasswordField.getPassword());

            if (ValidationUtil.isNullOrEmpty(name) || ValidationUtil.isNullOrEmpty(uName) || ValidationUtil.isNullOrEmpty(pWord)) {
                JOptionPane.showMessageDialog(dialog, "All fields are required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (!pWord.equals(cPWord)) {
                JOptionPane.showMessageDialog(dialog, "Passwords do not match.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (userDao.usernameExists(uName)) {
                JOptionPane.showMessageDialog(dialog, "Username already exists. Please choose a different one.", "Registration Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            boolean success = userDao.register(uName, pWord, name);
            if (success) {
                JOptionPane.showMessageDialog(dialog, "Account registered successfully!\nYou can now login with your credentials.", "Success", JOptionPane.INFORMATION_MESSAGE);
                usernameField.setText(uName);
                passwordField.setText("");
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "Failed to register account. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(registerSubmit);
        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }
}

