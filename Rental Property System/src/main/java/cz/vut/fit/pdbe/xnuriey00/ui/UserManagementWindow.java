package cz.vut.fit.pdbe.xnuriey00.ui;

import cz.vut.fit.pdbe.xnuriey00.dao.UserDAO;
import cz.vut.fit.pdbe.xnuriey00.model.User;
import cz.vut.fit.pdbe.xnuriey00.util.DBConnection;

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

    private JButton btnAdd, btnUpdate, btnDelete, btnClear;

    public UserManagementWindow(){

        super("Admin: User Management");

        // Window Setup
        setSize(800, 600);
        setLocationRelativeTo(null); // Center on screen
        // IMPORTANT: Use DISPOSE_ON_CLOSE so the main app stays open if you close this window
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        setLayout(new BorderLayout());
        // --- Top: Table ---
        String[] columns = {"ID", "Username", "Password Hash", "Type"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; } // Read-only table
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

        // --- Bottom: Form & Buttons ---
        JPanel bottomPanel = new JPanel(new GridLayout(5, 2));

        txtId = new JTextField(); txtId.setEditable(false); // ID is read-only
        txtUsername = new JTextField();
        txtPassword = new JTextField();
        txtType = new JTextField(); // Input "1" for Admin, "0" for User

        bottomPanel.add(new JLabel("ID:")); bottomPanel.add(txtId);
        bottomPanel.add(new JLabel("Username:")); bottomPanel.add(txtUsername);
        bottomPanel.add(new JLabel("Password:")); bottomPanel.add(txtPassword);
        bottomPanel.add(new JLabel("Type (1=Admin, 0=User):")); bottomPanel.add(txtType);

        JPanel btnPanel = new JPanel();
        btnAdd = new JButton("Create");
        btnUpdate = new JButton("Update");
        btnDelete = new JButton("Delete");
        btnClear = new JButton("Clear Form");

        btnPanel.add(btnAdd); btnPanel.add(btnUpdate); btnPanel.add(btnDelete); btnPanel.add(btnClear);

        JPanel container = new JPanel(new BorderLayout());
        container.add(bottomPanel, BorderLayout.CENTER);
        container.add(btnPanel, BorderLayout.SOUTH);
        add(container, BorderLayout.SOUTH);

        // --- Action Listeners ---
        btnAdd.addActionListener(e -> performAction("CREATE"));
        btnUpdate.addActionListener(e -> performAction("UPDATE"));
        btnDelete.addActionListener(e -> performAction("DELETE"));
        btnClear.addActionListener(e -> clearForm());

        // Load data initially
        loadData();
    }

    private void loadData() {
        tableModel.setRowCount(0); // Clear table
        try (Connection conn = DBConnection.getConnection()) {
            UserDAO dao = new UserDAO(conn);
            List<User> users = dao.getAllUsers();
            for (User u : users) {
                tableModel.addRow(new Object[]{u.getUserId(), u.getUsername(), u.getPasswordHash(), u.getUserType()});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void performAction(String action) {
        try (Connection conn = DBConnection.getConnection()) {
            UserDAO dao = new UserDAO(conn);

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
}
