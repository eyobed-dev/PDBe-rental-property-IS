package cz.vut.fit.pdbe.xnuriey00.ui;

import cz.vut.fit.pdbe.xnuriey00.queries.UserQueries;
import cz.vut.fit.pdbe.xnuriey00.model.User;
import cz.vut.fit.pdbe.xnuriey00.util.DBConnection;
import cz.vut.fit.pdbe.xnuriey00.util.UserSession;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.SQLException;

public class InitialWindow extends JFrame {
    private final JTextField txtUsername;
    private final JPasswordField txtPassword;

    public InitialWindow() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Login / Register");
        addWindowListener(
                new WindowAdapter() {
                    public void windowClosing(WindowEvent e) {
                        try {
                            DBConnection.close();
                        } catch (SQLException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                });
        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 5, 5));

        txtUsername = new JTextField(20);
        txtPassword = new JPasswordField(20);

        inputPanel.add(new JLabel("Username:"));
        inputPanel.add(txtUsername);
        inputPanel.add(new JLabel("Password:"));
        inputPanel.add(txtPassword);

        JPanel buttonPanel = new JPanel();
        JButton btnLogin = new JButton("Login");
        JButton btnRegister = new JButton("Register");

        btnLogin.addActionListener(e -> login());
        btnRegister.addActionListener(e -> register());

        buttonPanel.add(btnLogin);
        buttonPanel.add(btnRegister);

        getContentPane().add(inputPanel, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setResizable(false);
        setLocationRelativeTo(null);
    }

    // login existing user or administrator
    private void login() {
        try (Connection conn = DBConnection.getConnection()) {
            UserQueries userDAO = new UserQueries(conn);
            String user = txtUsername.getText().trim();
            String pass = new String(txtPassword.getPassword());

            User foundUser = userDAO.findByUsernameAndPassword(user, pass);

            if (true) {
                UserSession.getInstance().setCurrentUser(foundUser);
                new MainWindow().setVisible(true);
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid username or password", "Login Failed",
                        JOptionPane.ERROR_MESSAGE);
                txtPassword.setText("");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // Allow registering with normal user powers
    private void register() {
        try (Connection conn = DBConnection.getConnection()) {
            UserQueries userDAO = new UserQueries(conn);
            String user = txtUsername.getText().trim();
            String pass = new String(txtPassword.getPassword());

            if (user.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Username and password cannot be empty.", "Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Create new user with Default Type "0" (Standard User)
            User newUser = new User(user, pass, "0");
            userDAO.createUser(newUser);
            JOptionPane.showMessageDialog(this, "Registration successful!\nnow log in.", "User created",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException e) {
            if (e.getMessage().contains("unique constraint") || e.getErrorCode() == 1) {
                JOptionPane.showMessageDialog(this, "Username '" + txtUsername.getText() + "' is already taken.",
                        "Registration Failed", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
            try {
                //https://rychly-edu.gitlab.io/dbs/jdbc/#transactions
                //Bad User Information, undo
                DBConnection.getConnection().rollback();
            } catch (SQLException ignored) {
            }
        }
    }

}
