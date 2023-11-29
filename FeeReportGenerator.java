import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class FeeReportGenerator extends JFrame {
	private JTextField nameField, totalAmountField, totalPaidField, dueDateField;
    private JButton addButton, updateButton, deleteButton, generateReportButton;
    private JCheckBox viewCheckBox;
    private Connection connection;

    public FeeReportGenerator() {
        initializeUI();
        initializeDatabase();

        addButton.addActionListener(e -> addFeeReport());
        updateButton.addActionListener(e -> showUpdateDialog());
        deleteButton.addActionListener(e -> deleteFeeReport());
        generateReportButton.addActionListener(e -> generateReport());

        viewCheckBox.addActionListener(e -> {
            if (viewCheckBox.isSelected()) {
                generateReportButton.setEnabled(true);
            } else {
                generateReportButton.setEnabled(false);
            }
        });
    }

    private void initializeUI() {
        //user interface
        nameField = new JTextField(20);
        totalAmountField = new JTextField(10);
        totalPaidField = new JTextField(10);
        dueDateField = new JTextField(10);

        addButton = new JButton("Add");
        updateButton = new JButton("Update");
        deleteButton = new JButton("Delete");
        generateReportButton = new JButton("Generate Report");

        viewCheckBox = new JCheckBox("View Reports");

        setLayout(new FlowLayout());
        add(new JLabel("Name:"));
        add(nameField);
        add(new JLabel("Total Amount:"));
        add(totalAmountField);
        add(new JLabel("Total Paid:"));
        add(totalPaidField);
        add(new JLabel("Due Date (YYYY-MM-DD):"));
        add(dueDateField);
        add(addButton);
        add(updateButton);
        add(deleteButton);
        add(viewCheckBox);
        add(generateReportButton);

        setTitle("Fee Report Generator");
        setSize(400, 200);
        setLocationRelativeTo(null);
    }

    private void initializeDatabase() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String JDBC_URL = "jdbc:mysql://localhost:3306/fee_management";
            String JDBC_USER = "root";
            String JDBC_PASSWORD = "";
            connection = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error connecting to the database.", "Database Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void addFeeReport() {
        //adding report to db
        try {
            String name = nameField.getText();
            double totalAmount = Double.parseDouble(totalAmountField.getText());
            double totalPaid = Double.parseDouble(totalPaidField.getText());

            //due amount
            double dueAmount = totalAmount - totalPaid;

            String dueDateStr = dueDateField.getText();

            //date to string
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            java.util.Date dueDateUtil = sdf.parse(dueDateStr);
            java.sql.Date dueDate = new java.sql.Date(dueDateUtil.getTime());

            
            String sql = "INSERT INTO fee_reports (name, total_amount, total_paid, due_amount, due_date) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, name);
                statement.setDouble(2, totalAmount);
                statement.setDouble(3, totalPaid);
                statement.setDouble(4, dueAmount);
                statement.setDate(5, dueDate);

                int rowsInserted = statement.executeUpdate();
                if (rowsInserted > 0) {
                    JOptionPane.showMessageDialog(this, "Fee report added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    clearFields();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error adding fee report.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showUpdateDialog() {
        
        try {
            String nameToUpdate = JOptionPane.showInputDialog(this, "Enter the name to update:");

            if (nameToUpdate != null && !nameToUpdate.isEmpty()) {
                String sql = "SELECT * FROM fee_reports WHERE name=?";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, nameToUpdate);

                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (resultSet.next()) {
                            
                            double totalAmount = resultSet.getDouble("total_amount");
                            double totalPaid = resultSet.getDouble("total_paid");
                            String dueDate = resultSet.getString("due_date");

                            JTextField totalAmountFieldUpdate = new JTextField(Double.toString(totalAmount));
                            JTextField totalPaidFieldUpdate = new JTextField(Double.toString(totalPaid));
                            JTextField dueDateFieldUpdate = new JTextField(dueDate);

                            Object[] message = {
                                    "Total Amount:", totalAmountFieldUpdate,
                                    "Total Paid:", totalPaidFieldUpdate,
                                    "Due Date (YYYY-MM-DD):", dueDateFieldUpdate
                            };

                            int option = JOptionPane.showConfirmDialog(this, message, "Update Fee Report", JOptionPane.OK_CANCEL_OPTION);
                            if (option == JOptionPane.OK_OPTION) {
                                
                                updateFeeReport(nameToUpdate, Double.parseDouble(totalAmountFieldUpdate.getText()), Double.parseDouble(totalPaidFieldUpdate.getText()), dueDateFieldUpdate.getText());
                            }
                        } else {
                            JOptionPane.showMessageDialog(this, "No such fee report found.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating fee report.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateFeeReport(String name, double totalAmount, double totalPaid, String dueDateStr) {
        //update existing report in db
        

        try {
            //due amount
            double dueAmount = totalAmount - totalPaid;

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            java.util.Date dueDateUtil = sdf.parse(dueDateStr);
            java.sql.Date dueDate = new java.sql.Date(dueDateUtil.getTime());

            String sql = "UPDATE fee_reports SET total_amount=?, total_paid=?, due_amount=?, due_date=? WHERE name=?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setDouble(1, totalAmount);
                statement.setDouble(2, totalPaid);
                statement.setDouble(3, dueAmount);
                statement.setDate(4, dueDate);
                statement.setString(5, name);

                int rowsUpdated = statement.executeUpdate();
                if (rowsUpdated > 0) {
                    JOptionPane.showMessageDialog(this, "Fee report updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    clearFields();
                } else {
                    JOptionPane.showMessageDialog(this, "No such fee report found.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating fee report.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteFeeReport() {
        //delete fee report
        try {
            String name = nameField.getText();

            //
            String sql = "DELETE FROM fee_reports WHERE name = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, name);

                int rowsDeleted = statement.executeUpdate();
                if (rowsDeleted > 0) {
                    JOptionPane.showMessageDialog(this, "Fee report deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    clearFields();
                } else {
                    JOptionPane.showMessageDialog(this, "No such fee report found.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error deleting fee report.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void generateReport() {
        //generate report
        try {
            String sql = "SELECT * FROM fee_reports";
            try (Statement statement = connection.createStatement()) {
                try (ResultSet resultSet = statement.executeQuery(sql)) {
                    List<String> reportData = new ArrayList<>();
                    while (resultSet.next()) {
                        String name = resultSet.getString("name");
                        double totalAmount = resultSet.getDouble("total_amount");
                        double totalPaid = resultSet.getDouble("total_paid");
                        double dueAmount = resultSet.getDouble("due_amount");
                        String dueDate = resultSet.getString("due_date");

                        String reportEntry = String.format("Name: %s, Total Amount: %.2f, Total Paid: %.2f, Due Amount: %.2f, Due Date: %s",
                                name, totalAmount, totalPaid, dueAmount, dueDate);
                        reportData.add(reportEntry);
                    }

                    if (!reportData.isEmpty()) {
                        //display report
                        JTextArea reportTextArea = new JTextArea(String.join("\n", reportData));
                        reportTextArea.setEditable(false);
                        JScrollPane scrollPane = new JScrollPane(reportTextArea);

                        JFrame reportFrame = new JFrame("Fee Report");
                        reportFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                        reportFrame.setSize(400, 300);
                        reportFrame.setLocationRelativeTo(null);
                        reportFrame.add(scrollPane);
                        reportFrame.setVisible(true);
                    } else {
                        JOptionPane.showMessageDialog(this, "No fee reports found.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error generating report.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearFields() {
        nameField.setText("");
        totalAmountField.setText("");
        totalPaidField.setText("");
        dueDateField.setText("");
    }

}
