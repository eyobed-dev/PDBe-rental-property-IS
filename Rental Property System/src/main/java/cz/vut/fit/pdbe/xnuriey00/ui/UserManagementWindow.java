package cz.vut.fit.pdbe.xnuriey00.ui;

import cz.vut.fit.pdbe.xnuriey00.Main;
import cz.vut.fit.pdbe.xnuriey00.queries.UserQueries;
import cz.vut.fit.pdbe.xnuriey00.model.User;
import cz.vut.fit.pdbe.xnuriey00.util.DBConnection;
import cz.vut.fit.pdbe.xnuriey00.util.SQLScriptExecutor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class UserManagementWindow extends JFrame {
    private JTable table;
    private DefaultTableModel tableModel;

    // Form Fields
    private JTextField txtId, txtUsername, txtPassword, txtType;

    private JButton btnAdd, btnUpdate, btnDelete, btnClear, btnReinitializeDB;

    public UserManagementWindow() {

        super("Admin: User Management");

        // Window Setup
        setSize(800, 600);
        setLocationRelativeTo(null); // Center on screen
        // so the main app stays open if I close this window
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        setLayout(new BorderLayout());
        // --- Top Table ---
        String[] columns = { "ID", "Username", "Password Hash", "Type" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            } // Read-only table
        };
        table = new JTable(tableModel);

        // Populate form on row click
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                txtId.setText(tableModel.getValueAt(row, 0).toString());
                txtUsername.setText(tableModel.getValueAt(row, 1).toString());
                txtPassword.setText(tableModel.getValueAt(row, 2).toString());
                txtType.setText(tableModel.getValueAt(row, 3).toString());
            }
        });

        add(new JScrollPane(table), BorderLayout.CENTER);
        // --- Bottom Form and Buttons ---
        JPanel bottomPanel = new JPanel(new GridLayout(5, 2));

        txtId = new JTextField();
        txtId.setEditable(false); // ID is read only
        txtUsername = new JTextField();
        txtPassword = new JTextField();
        txtType = new JTextField(); // 1 for admin, 0 for user

        bottomPanel.add(new JLabel("ID:"));
        bottomPanel.add(txtId);
        bottomPanel.add(new JLabel("Username:"));
        bottomPanel.add(txtUsername);
        bottomPanel.add(new JLabel("Password:"));
        bottomPanel.add(txtPassword);
        bottomPanel.add(new JLabel("Type 1 for admin, 0 for user:"));
        bottomPanel.add(txtType);

        JPanel btnPanel = new JPanel();
        btnAdd = new JButton("Create");
        btnUpdate = new JButton("Update");
        btnDelete = new JButton("Delete");
        btnClear = new JButton("Clear Form");

        btnPanel.add(btnAdd);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);
        btnPanel.add(btnClear);

        // Database reinitialization button (admin only)
        JPanel dbPanel = new JPanel();
        btnReinitializeDB = new JButton("⚠ Reinitialize Database");
        btnReinitializeDB.setBackground(new Color(255, 0, 23, 255)); // Red background
        btnReinitializeDB.setForeground(Color.WHITE);
        btnReinitializeDB.setFocusPainted(false);
        dbPanel.add(new JLabel("Admin Actions:"));
        dbPanel.add(btnReinitializeDB);

        JPanel container = new JPanel(new BorderLayout());
        container.add(bottomPanel, BorderLayout.CENTER);
        container.add(btnPanel, BorderLayout.SOUTH);

        JPanel southContainer = new JPanel(new BorderLayout());
        southContainer.add(container, BorderLayout.CENTER);
        southContainer.add(dbPanel, BorderLayout.SOUTH);
        add(southContainer, BorderLayout.SOUTH);

        // --- Action Listeners ---
        btnAdd.addActionListener(e -> performAction("CREATE"));
        btnUpdate.addActionListener(e -> performAction("UPDATE"));
        btnDelete.addActionListener(e -> performAction("DELETE"));
        btnClear.addActionListener(e -> clearForm());
        btnReinitializeDB.addActionListener(e -> reinitializeDatabase());

        // Load data initially
        loadData();
    }

    private void loadData() {
        tableModel.setRowCount(0); // Clear table
        try (Connection conn = DBConnection.getConnection()) {
            UserQueries dao = new UserQueries(conn);
            List<User> users = dao.getAllUsers();
            for (User u : users) {
                tableModel
                        .addRow(new Object[] { u.getUserId(), u.getUsername(), u.getPasswordHash(), u.getUserType() });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void performAction(String action) {
        try (Connection conn = DBConnection.getConnection()) {
            UserQueries dao = new UserQueries(conn);

            String uName = txtUsername.getText();
            String uPass = txtPassword.getText();
            String uType = txtType.getText();

            switch (action) {
                case "CREATE":
                    dao.createUser(new User(uName, uPass, uType));
                    conn.commit();
                    break;
                case "UPDATE":
                    Long id = Long.parseLong(txtId.getText());
                    User u = new User(id, uName, uPass, uType);
                    dao.updateUser(u);
                    conn.commit();
                    break;
                case "DELETE":
                    Long delId = Long.parseLong(txtId.getText());
                    dao.deleteUser(delId);
                    conn.commit();
                    break;
            }
            // Refresh table
            loadData();
            clearForm();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void clearForm() {
        txtId.setText("");
        txtUsername.setText("");
        txtPassword.setText("");
        txtType.setText("");
        table.clearSelection();
    }

    /**
     * Reinitializes the database by executing schema.sql.
     */
    private void reinitializeDatabase() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "WARNING: This will DROP all tables and DELETE all data!\n" +
                        "Proceed?",
                "Confirm Database Reinitialization",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) {
            return; // User cancelled
        }

        try (Connection conn = DBConnection.getConnection()) {
            List<String> errors = SQLScriptExecutor.executeScript(conn, "schema.sql");

            if (errors.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Database reinitialized.\nReturning to login screen.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);

                // Close all windows and return to initial window
                closeAllWindowsAndRestart();

            } else {
                StringBuilder errorMsg = new StringBuilder("Database reinitialized with some errors:\n\n");
                for (String error : errors) {
                    errorMsg.append("• ").append(error).append("\n");
                }
                JOptionPane.showMessageDialog(
                        this,
                        errorMsg.toString(),
                        "Partial Success",
                        JOptionPane.WARNING_MESSAGE);

                // Still close and restart even with errors
                closeAllWindowsAndRestart();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Failed to reinitialize database:\n" + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Closes all open windows and returns to the initial login window.
     */
    private void closeAllWindowsAndRestart() {
        // Close all windows
        for (Window window : Window.getWindows()) {
            window.dispose();
        }

        // Restart the application
        SwingUtilities.invokeLater(Main::launch);
    }
}
