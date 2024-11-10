import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class WaterLevelAlertSystem extends JFrame {

    // GUI components
    private JTextField damNameField;
    private JTable resultTable;
    private JButton checkButton;

    // Database credentials
    private static final String DB_URL = "jdbc:mysql://localhost:3306/flood_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "krishna@123";

    // Sediment percentage (e.g., 10% sediment, so 90% water)
    private static final float SEDIMENT_PERCENTAGE = 0.10f;

    public WaterLevelAlertSystem() {
        setTitle("Dam Water Level Alert System");
        setSize(1200, 500);  // Increased size to fit additional columns
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the window

        // Layout setup
        setLayout(new BorderLayout());

        // Dam name input panel
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new FlowLayout());

        JLabel damNameLabel = new JLabel("Enter Dam Name: ");
        damNameField = new JTextField(20);
        checkButton = new JButton("Check Water Level");

        inputPanel.add(damNameLabel);
        inputPanel.add(damNameField);
        inputPanel.add(checkButton);

        // Table to display results with additional columns
        String[] columnNames = {"Dam Name", "Water Level (feet)", "Water Increase (feet)", "Water Release (feet)", "Water Volume (excluding Sediment)", "Reason", "Last Updated"};
        resultTable = new JTable(new DefaultTableModel(columnNames, 0));
        JScrollPane tableScrollPane = new JScrollPane(resultTable);

        // Add components to the frame
        add(inputPanel, BorderLayout.NORTH);
        add(tableScrollPane, BorderLayout.CENTER);

        // Copyright label at the bottom
        JLabel copyrightLabel = new JLabel("© Usham Roy, 2024", SwingConstants.CENTER);
        add(copyrightLabel, BorderLayout.SOUTH);

        // Set button action
        checkButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                checkWaterLevel();
            }
        });

        // Set custom cell renderer for the "Water Level" column
        resultTable.getColumnModel().getColumn(1).setCellRenderer(new WaterLevelCellRenderer());
    }

    // Method to fetch water level logs and display them in the table
    private void checkWaterLevel() {
        String damName = damNameField.getText();
        if (damName.isEmpty()) {
            JOptionPane.showMessageDialog(WaterLevelAlertSystem.this, "Please enter the dam name.");
            return;
        }

        // Database connection and query
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT fa.total_water_level, fa.water_level_increase, fa.water_release, fa.reason, fa.last_updated, d.max_capacity " +
                             "FROM dams d " +
                             "JOIN flood_alerts fa ON d.dam_name = fa.dam_name " +
                             "WHERE LOWER(d.dam_name) = LOWER(?) ORDER BY fa.last_updated DESC")) {

            stmt.setString(1, damName);

            ResultSet rs = stmt.executeQuery();

            // Check if records are found and process result set
            boolean recordsFound = false;
            DefaultTableModel tableModel = (DefaultTableModel) resultTable.getModel();
            tableModel.setRowCount(0);  // Clear existing rows

            // Loop through the result set and add data to the table
            while (rs.next()) {
                recordsFound = true;

                float waterLevel = rs.getFloat("total_water_level");
                float waterIncrease = rs.getFloat("water_level_increase");
                float waterRelease = rs.getFloat("water_release");
                String reason = rs.getString("reason");
                Timestamp lastUpdated = rs.getTimestamp("last_updated");
                float maxCapacity = rs.getFloat("max_capacity");

                // Calculate water volume excluding sediment
                float waterVolumeExcludingSediment = waterLevel * (1 - SEDIMENT_PERCENTAGE);

                // Add fetched data and computed volume to the table
                tableModel.addRow(new Object[]{damName, waterLevel, waterIncrease, waterRelease, waterVolumeExcludingSediment, reason, lastUpdated});

                // Dynamically set thresholds within the renderer
                ((WaterLevelCellRenderer) resultTable.getColumnModel().getColumn(1).getCellRenderer())
                        .setThresholds(maxCapacity * 0.50f, maxCapacity * 0.70f, maxCapacity * 0.90f, maxCapacity);
            }

            if (!recordsFound) {
                JOptionPane.showMessageDialog(WaterLevelAlertSystem.this, "Dam not found in the database.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(WaterLevelAlertSystem.this, "Error fetching data from the database.");
        }
    }

    // Custom cell renderer for the Water Level column
    private static class WaterLevelCellRenderer extends DefaultTableCellRenderer {
        private float greenThreshold;
        private float yellowThreshold;
        private float orangeThreshold;
        private float redThreshold;

        public void setThresholds(float green, float yellow, float orange, float red) {
            this.greenThreshold = green;
            this.yellowThreshold = yellow;
            this.orangeThreshold = orange;
            this.redThreshold = red;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (value instanceof Float) {
                float waterLevel = (Float) value;
                if (waterLevel <= greenThreshold) {
                    cell.setBackground(Color.GREEN);
                    cell.setForeground(Color.BLACK);
                } else if (waterLevel <= yellowThreshold) {
                    cell.setBackground(Color.YELLOW);
                    cell.setForeground(Color.BLACK);
                } else if (waterLevel <= orangeThreshold) {
                    cell.setBackground(Color.ORANGE);
                    cell.setForeground(Color.BLACK);
                } else if (waterLevel <= redThreshold) {
                    cell.setBackground(Color.RED);
                    cell.setForeground(Color.WHITE);
                } else {
                    cell.setBackground(Color.BLACK);
                    cell.setForeground(Color.WHITE);
                }
            } else {
                cell.setBackground(Color.WHITE);
                cell.setForeground(Color.BLACK);
            }
            return cell;
        }
    }

    // Main method to run the application
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new WaterLevelAlertSystem().setVisible(true));
    }
}
