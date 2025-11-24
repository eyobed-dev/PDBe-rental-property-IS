package cz.vut.fit.pdbe.xnuriey00.ui;

import cz.vut.fit.pdbe.xnuriey00.dao.UserDAO;
import cz.vut.fit.pdbe.xnuriey00.model.User;
import cz.vut.fit.pdbe.xnuriey00.util.DBConnection;
import cz.vut.fit.pdbe.xnuriey00.util.UserSession;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;

public class InitialWindow extends JFrame {
    private final JTextField txtUsername;
    private final JPasswordField txtPassword;

    public InitialWindow() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Login / Register");

        // Panel for username and password fields
        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 5, 5)); // 2 rows, 2 columns, with 5px hgap and vgap

        txtUsername = new JTextField(20);
        txtPassword = new JPasswordField(20);

        inputPanel.add(new JLabel("Username:"));
        inputPanel.add(txtUsername);
        inputPanel.add(new JLabel("Password:"));
        inputPanel.add(txtPassword);

        JPanel buttonPanel = new JPanel();
        JButton btnLogin = new JButton("Login");
        JButton btnRegister = new JButton("Register");
        JButton btnExit = new JButton("Exit");

        btnLogin.addActionListener(e -> attemptLogin());
        btnRegister.addActionListener(e -> attemptRegister());
        btnExit.addActionListener(e -> System.exit(0));

        buttonPanel.add(btnLogin);
        buttonPanel.add(btnRegister);
        buttonPanel.add(btnExit);

        getContentPane().add(inputPanel, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH); // Or BorderLayout.PAGE_END

        pack();
        setResizable(false);
        setLocationRelativeTo(null);
    }

    //login existing user or administrator
    private void attemptLogin() {
        try (Connection conn = DBConnection.getConnection()) {
            UserDAO userDAO = new UserDAO(conn);
            String user = txtUsername.getText().trim();
            String pass = new String(txtPassword.getPassword());

            User foundUser = userDAO.findByUsernameAndPassword(user, pass);

            if (foundUser != null) {
                // 1. Store User in Session
                UserSession.getInstance().setCurrentUser(foundUser);

                // 2. Open Main Frame
                new MainWindow().setVisible(true);

                // 3. Close Login Frame
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid username or password", "Login Failed", JOptionPane.ERROR_MESSAGE);
                txtPassword.setText("");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    //Allow registering with normal user powers
    private void attemptRegister() {
        try (Connection conn = DBConnection.getConnection()) {
            UserDAO userDAO = new UserDAO(conn);
            String user = txtUsername.getText().trim();
            String pass = new String(txtPassword.getPassword());

            if (user.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Username and password cannot be empty.", "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Create new user with Default Type "0" (Standard User)
            User newUser = new User(user, pass, "0");
            userDAO.createUser(newUser);

            conn.commit(); // commit for Oracle

            JOptionPane.showMessageDialog(this, "Registration successful!\nYou can now log in.", "Success", JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException e) {
            e.printStackTrace();
            if (e.getMessage().contains("unique constraint") || e.getErrorCode() == 1) {
                JOptionPane.showMessageDialog(this, "Username '" + txtUsername.getText() + "' is already taken.", "Registration Failed", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
            try {
                DBConnection.getConnection();
                DBConnection.getConnection().rollback();
            } catch (SQLException ex) { ex.printStackTrace(); }
        }
    }

}
