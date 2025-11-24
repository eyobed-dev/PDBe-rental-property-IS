package cz.vut.fit;

import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import java.awt.GridLayout;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("flatlaf fail");
        }

        // Thread problem solution if the db is stuck
        SwingUtilities.invokeLater(() -> {

            // Simple Login
            JPanel panel = new JPanel(new GridLayout(2, 2));
            JTextField user = new JTextField("xnuriey00"); // Prefill for ease
            JPasswordField pass = new JPasswordField();

            panel.add(new JLabel("User:"));
            panel.add(user);
            panel.add(new JLabel("Pass:"));
            panel.add(pass);
            // Ditched the service ID since it doesn't change. hard coded in DB.java

            int result = JOptionPane.showConfirmDialog(null, panel, "Oracle Login", JOptionPane.OK_CANCEL_OPTION);

            if (result == JOptionPane.OK_OPTION) {
                try {
                    //DB.connect(user.getText(), new String(pass.getPassword()));
                    new UI().setVisible(true);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "Fail\n\n " + e.getMessage());
                }
            }
        });
    }
}