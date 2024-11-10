import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FloodAlertAdminSystem extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainPanel;

    // First interface components
    private JPanel damCheckPanel;
    private JButton submitButton;
    private JTextField damNameField;

    // Second interface components (for existing or new dam)
    private JPanel damDetailsPanel;
    private JTextField damNameFieldRegistration;
    private JTextField waterLevelField, dateField, timeField, lastUpdatedField, maxCapacityField;
    private JTextField waterLevelIncreaseField, waterReleaseField;
    private JComboBox<String> reasonComboBox;
    private JTextArea resultArea;
    private JButton submitDataButton;

    // Database credentials
    private static final String DB_URL = "jdbc:mysql://localhost:3306/flood_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "krishna@123";

    public FloodAlertAdminSystem() {
        setTitle("Flood Alert System - Admin Panel");
        setSize(500, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the window
        setLayout(new BorderLayout());

        // Initialize CardLayout
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Build the first interface (dam availability check)
        buildDamCheckPanel();

        // Build the second interface (dam details form)
        buildDamDetailsPanel();

        // Add both panels to the CardLayout container
        mainPanel.add(damCheckPanel, "damCheckPanel");
        mainPanel.add(damDetailsPanel, "damDetailsPanel");

        add(mainPanel);
    }

    private void buildDamCheckPanel() {
        damCheckPanel = new JPanel();
        damCheckPanel.setLayout(new BoxLayout(damCheckPanel, BoxLayout.Y_AXIS));
        damCheckPanel.setBorder(new EmptyBorder(50, 50, 50, 50));

        JLabel questionLabel = new JLabel("Check if the dam is already registered:");
        questionLabel.setFont(new Font("Arial", Font.BOLD, 18));
        questionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        damNameField = new JTextField();
        damNameField.setMaximumSize(new Dimension(400, 30));
        damNameField.setToolTipText("Enter Dam Name");

        submitButton = createStyledButton("Check Dam", new Color(70, 130, 180));

        damCheckPanel.add(questionLabel);
        damCheckPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        damCheckPanel.add(new JLabel("Enter Dam Name:"));
        damCheckPanel.add(damNameField);
        damCheckPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        damCheckPanel.add(submitButton);

        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                checkDam();
            }
        });
    }

    private void buildDamDetailsPanel() {
        damDetailsPanel = new JPanel(new GridBagLayout());
        damDetailsPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        damNameFieldRegistration = new JTextField();
        addLabelAndField(damDetailsPanel, gbc, "Dam Name (for new registration):", damNameFieldRegistration);
        damNameFieldRegistration.setVisible(false);

        waterLevelField = new JTextField();
        waterLevelField.setEditable(false);
        addLabelAndField(damDetailsPanel, gbc, "Current Water Level (feet):", waterLevelField);

        lastUpdatedField = new JTextField();
        lastUpdatedField.setEditable(false);
        addLabelAndField(damDetailsPanel, gbc, "Last Updated:", lastUpdatedField);

        maxCapacityField = new JTextField();
        maxCapacityField.setEditable(false); // Make maxCapacityField non-editable
        maxCapacityField.setToolTipText("Maximum capacity (non-editable)");
        addLabelAndField(damDetailsPanel, gbc, "Maximum Capacity (feet):", maxCapacityField);

        waterLevelIncreaseField = new JTextField();
        addLabelAndField(damDetailsPanel, gbc, "Water Level Increase (feet):", waterLevelIncreaseField);

        waterReleaseField = new JTextField();
        addLabelAndField(damDetailsPanel, gbc, "Water Release (feet):", waterReleaseField);

        String[] reasons = {"Heavy Rainfall", "Snow/Ice Melting", "Upstream Flooding", "Dam Release from Upstream", "Sediment Collection"};
        reasonComboBox = new JComboBox<>(reasons);
        addLabelAndField(damDetailsPanel, gbc, "Reason:", reasonComboBox);

        dateField = new JTextField();
        dateField.setEditable(false);
        addLabelAndField(damDetailsPanel, gbc, "Date (yyyy-MM-dd):", dateField);

        timeField = new JTextField();
        timeField.setEditable(false);
        addLabelAndField(damDetailsPanel, gbc, "Time (HH:mm):", timeField);

        submitDataButton = createStyledButton("Submit Data", new Color(70, 130, 180));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.add(submitDataButton);

        resultArea = new JTextArea(6, 30);
        resultArea.setEditable(false);
        resultArea.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        resultArea.setBackground(Color.WHITE);
        resultArea.setFont(new Font("Arial", Font.PLAIN, 14));

        gbc.gridx = 0;
        gbc.gridy = 12;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.CENTER;
        damDetailsPanel.add(buttonPanel, gbc);

        gbc.gridy = 13;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        damDetailsPanel.add(new JScrollPane(resultArea), gbc);

        submitDataButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                submitData();
            }
        });
    }

    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void addLabelAndField(JPanel panel, GridBagConstraints gbc, String labelText, JComponent field) {
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 1;
        panel.add(new JLabel(labelText), gbc);

        gbc.gridx = 1;
        panel.add(field, gbc);
    }

    private void checkDam() {
        String damName = damNameField.getText();
        if (damName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a dam name.");
            return;
        }

        String sql = "SELECT total_water_level, last_updated, max_capacity FROM dams d " +
                "JOIN flood_alerts fa ON d.dam_name = fa.dam_name WHERE LOWER(d.dam_name) = LOWER(?) ORDER BY fa.alert_id DESC LIMIT 1";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, damName);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                waterLevelField.setText(rs.getString("total_water_level"));
                lastUpdatedField.setText(rs.getString("last_updated"));
                maxCapacityField.setText(rs.getString("max_capacity"));
                resultArea.setText("Dam details found. You can submit a new water level increase or release water.");

                setCurrentDateTime();

                cardLayout.show(mainPanel, "damDetailsPanel");
                damNameFieldRegistration.setVisible(false);
            } else {
                JOptionPane.showMessageDialog(this, "Dam not found. Please provide details for a new dam.");
                resetForm();
                damNameFieldRegistration.setText(damName);
                setCurrentDateTime();
                cardLayout.show(mainPanel, "damDetailsPanel");
                damNameFieldRegistration.setVisible(true);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void resetForm() {
        waterLevelField.setText("");
        lastUpdatedField.setText("");
        maxCapacityField.setText("");
        waterLevelIncreaseField.setText("");
        waterReleaseField.setText("");
        reasonComboBox.setSelectedIndex(0);
        resultArea.setText("");
    }

    private void submitData() {
        String damName = damNameField.getText();
        if (damNameFieldRegistration.isVisible()) {
            damName = damNameFieldRegistration.getText();
        }
        String maxCapacity = maxCapacityField.getText();

        float currentWaterLevel = 0.0f;
        if (!waterLevelField.getText().isEmpty()) {
            try {
                currentWaterLevel = Float.parseFloat(waterLevelField.getText());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Error reading the current water level.");
                return;
            }
        }

        float waterLevelIncrease = 0.0f;
        float waterRelease = 0.0f;
        try {
            waterLevelIncrease = Float.parseFloat(waterLevelIncreaseField.getText());
            waterRelease = Float.parseFloat(waterReleaseField.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers for water level increase and release.");
            return;
        }

        String reason = (String) reasonComboBox.getSelectedItem();
        String date = dateField.getText();
        String time = timeField.getText();

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String checkDamSql = "SELECT dam_id FROM dams WHERE dam_name = ?";
            PreparedStatement checkDamStmt = conn.prepareStatement(checkDamSql);
            checkDamStmt.setString(1, damName);
            ResultSet damResult = checkDamStmt.executeQuery();

            int damId = -1;
            if (damResult.next()) {
                damId = damResult.getInt("dam_id");
            } else {
                if (maxCapacity.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please enter max capacity for new dam.");
                    return;
                }

                String insertDamSql = "INSERT INTO dams (dam_name, max_capacity) VALUES (?, ?)";
                PreparedStatement insertDamStmt = conn.prepareStatement(insertDamSql, Statement.RETURN_GENERATED_KEYS);
                insertDamStmt.setString(1, damName);
                insertDamStmt.setFloat(2, Float.parseFloat(maxCapacity));
                insertDamStmt.executeUpdate();

                ResultSet generatedKeys = insertDamStmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    damId = generatedKeys.getInt(1);
                }
                JOptionPane.showMessageDialog(this, "New dam registered successfully!");
            }

            // Calculate new total water level
            float newTotalWaterLevel = currentWaterLevel + waterLevelIncrease - waterRelease;
            waterLevelField.setText(String.valueOf(newTotalWaterLevel));

            // Insert the new flood alert data
            String insertAlertSql = "INSERT INTO flood_alerts (dam_name, total_water_level, water_level_increase, water_release, reason, date, time) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement insertAlertStmt = conn.prepareStatement(insertAlertSql);
            insertAlertStmt.setString(1, damName);
            insertAlertStmt.setFloat(2, newTotalWaterLevel); // Use the updated total water level here
            insertAlertStmt.setFloat(3, waterLevelIncrease);
            insertAlertStmt.setFloat(4, waterRelease);
            insertAlertStmt.setString(5, reason);
            insertAlertStmt.setString(6, date);
            insertAlertStmt.setString(7, time);
            insertAlertStmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Data submitted successfully!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void setCurrentDateTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        dateField.setText(now.format(dateFormatter));
        timeField.setText(now.format(timeFormatter));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new FloodAlertAdminSystem().setVisible(true);
            }
        });
    }
}
