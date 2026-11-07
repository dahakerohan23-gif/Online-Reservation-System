package com.reservation.ui;

import com.reservation.dao.TrainDao;
import com.reservation.model.Train;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

public class TrainDirectoryPanel extends JPanel {

    private final TrainDao trainDao = new TrainDao();
    private final Consumer<Train> onTrainSelectedForBooking;

    private JTable trainsTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField searchField;

    public TrainDirectoryPanel(Consumer<Train> onTrainSelectedForBooking) {
        this.onTrainSelectedForBooking = onTrainSelectedForBooking;
        setLayout(new BorderLayout());
        setBackground(UIStyle.BG_LIGHT);
        setBorder(new EmptyBorder(20, 25, 20, 25));

        initUI();
        loadTrains();
    }

    private void initUI() {
        JPanel contentCard = new JPanel(new BorderLayout(0, 15));
        contentCard.setBackground(UIStyle.CARD_BG);
        contentCard.setBorder(UIStyle.createCardBorder());

        // Header + Search Toolbar
        JPanel topPanel = new JPanel(new BorderLayout(15, 10));
        topPanel.setOpaque(false);

        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setOpaque(false);

        JLabel titleLabel = new JLabel("Train Schedule & Route Directory");
        titleLabel.setFont(UIStyle.FONT_TITLE);
        titleLabel.setForeground(UIStyle.PRIMARY);

        JLabel subtitleLabel = new JLabel("Select any train from the schedule to automatically populate the reservation form.");
        subtitleLabel.setFont(UIStyle.FONT_REGULAR);
        subtitleLabel.setForeground(UIStyle.TEXT_MUTED);

        titleBox.add(titleLabel);
        titleBox.add(Box.createRigidArea(new Dimension(0, 4)));
        titleBox.add(subtitleLabel);

        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        toolBar.setOpaque(false);

        JLabel searchLabel = new JLabel("Search Train / Station:");
        searchLabel.setFont(UIStyle.FONT_BOLD);
        searchLabel.setForeground(UIStyle.TEXT_MAIN);

        searchField = UIStyle.createTextField();
        searchField.setPreferredSize(new Dimension(200, 34));
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
        });

        JButton bookSelectedBtn = UIStyle.createPrimaryButton("Book Selected Train");
        bookSelectedBtn.addActionListener(e -> bookSelected());

        toolBar.add(searchLabel);
        toolBar.add(searchField);
        toolBar.add(bookSelectedBtn);

        topPanel.add(titleBox, BorderLayout.WEST);
        topPanel.add(toolBar, BorderLayout.EAST);

        contentCard.add(topPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"Train Number", "Train Name", "Origin / Source", "Destination"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        trainsTable = new JTable(tableModel);
        trainsTable.setFont(UIStyle.FONT_REGULAR);
        trainsTable.setRowHeight(32);
        trainsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        trainsTable.getTableHeader().setFont(UIStyle.FONT_BOLD);
        trainsTable.getTableHeader().setBackground(new Color(241, 245, 249));
        trainsTable.getTableHeader().setForeground(UIStyle.TEXT_MAIN);
        trainsTable.setShowGrid(true);
        trainsTable.setGridColor(UIStyle.BORDER_COLOR);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        trainsTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);

        sorter = new TableRowSorter<>(tableModel);
        trainsTable.setRowSorter(sorter);

        JScrollPane scrollPane = new JScrollPane(trainsTable);
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

    private void loadTrains() {
        tableModel.setRowCount(0);
        List<Train> trains = trainDao.getAllTrains();
        for (Train t : trains) {
            tableModel.addRow(new Object[]{
                    t.getTrainNumber(),
                    t.getTrainName(),
                    t.getSource(),
                    t.getDestination()
            });
        }
    }

    private void bookSelected() {
        int row = trainsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a train from the directory first.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int modelRow = trainsTable.convertRowIndexToModel(row);
        int trainNumber = (Integer) tableModel.getValueAt(modelRow, 0);
        String trainName = (String) tableModel.getValueAt(modelRow, 1);
        String source = (String) tableModel.getValueAt(modelRow, 2);
        String dest = (String) tableModel.getValueAt(modelRow, 3);

        Train train = new Train(trainNumber, trainName, source, dest);
        if (onTrainSelectedForBooking != null) {
            onTrainSelectedForBooking.accept(train);
        }
    }
}

