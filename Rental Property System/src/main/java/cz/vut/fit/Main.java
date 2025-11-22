package cz.vut.fit;

import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import java.awt.GridLayout;

public class Main {
    public static void main(String[] args) {
        FlatLightLaf.setup(); // Make it look nice

        //Thread problem solution if the db is stuck
        SwingUtilities.invokeLater(() -> {

            // Simple Login
            JPanel panel = new JPanel(new GridLayout(2, 2));
            JTextField user = new JTextField("xnuriey00"); // Prefill for ease
            JPasswordField pass = new JPasswordField();


            panel.add(new JLabel("User:")); panel.add(user);
            panel.add(new JLabel("Pass:")); panel.add(pass);

            int result = JOptionPane.showConfirmDialog(null, panel, "Oracle Login", JOptionPane.OK_CANCEL_OPTION);

            if (result == JOptionPane.OK_OPTION) {
                try {
                    DB.connect(user.getText(), new String(pass.getPassword()));
                    new UI();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "Fail\n\n " + e.getMessage());
                }
            }
        });
    }
}