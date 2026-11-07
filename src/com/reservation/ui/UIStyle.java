package com.reservation.ui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public class UIStyle {

    // Color Palette
    public static final Color PRIMARY = new Color(26, 60, 120);       // Rich Navy
    public static final Color PRIMARY_HOVER = new Color(37, 99, 235); // Royal Blue
    public static final Color ACCENT = new Color(13, 148, 136);       // Teal
    public static final Color DANGER = new Color(220, 38, 38);        // Crimson Red
    public static final Color DANGER_HOVER = new Color(185, 28, 28);
    public static final Color SUCCESS = new Color(22, 163, 74);       // Green
    public static final Color BG_LIGHT = new Color(245, 247, 250);    // Light Gray BG
    public static final Color CARD_BG = Color.WHITE;
    public static final Color TEXT_MAIN = new Color(30, 41, 59);      // Slate 800
    public static final Color TEXT_MUTED = new Color(100, 116, 139);  // Slate 500
    public static final Color BORDER_COLOR = new Color(226, 232, 240);// Light Slate Border

    // Fonts
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_SUBHEADER = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_MONO = new Font("Consolas", Font.BOLD, 14);

    public static JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setForeground(Color.WHITE);
        btn.setBackground(PRIMARY);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(PRIMARY_HOVER);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(PRIMARY);
            }
        });
        return btn;
    }

    public static JButton createDangerButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setForeground(Color.WHITE);
        btn.setBackground(DANGER);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(DANGER_HOVER);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(DANGER);
            }
        });
        return btn;
    }

    public static JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_REGULAR);
        btn.setForeground(TEXT_MAIN);
        btn.setBackground(new Color(241, 245, 249));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new CompoundBorder(new LineBorder(BORDER_COLOR, 1), new EmptyBorder(8, 16, 8, 16)));
        return btn;
    }

    public static JTextField createTextField() {
        JTextField tf = new JTextField();
        tf.setFont(FONT_REGULAR);
        tf.setForeground(TEXT_MAIN);
        tf.setBackground(Color.WHITE);
        tf.setCaretColor(PRIMARY);
        tf.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));
        return tf;
    }

    public static JPasswordField createPasswordField() {
        JPasswordField pf = new JPasswordField();
        pf.setFont(FONT_REGULAR);
        pf.setForeground(TEXT_MAIN);
        pf.setBackground(Color.WHITE);
        pf.setCaretColor(PRIMARY);
        pf.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));
        return pf;
    }

    public static Border createCardBorder() {
        return new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1, true),
                new EmptyBorder(16, 20, 16, 20)
        );
    }
}

