package cz.vut.fit.pdbe.xnuriey00;

import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;

import cz.vut.fit.pdbe.xnuriey00.ui.InitialWindow;
import cz.vut.fit.pdbe.xnuriey00.util.DBConnection;
import oracle.jdbc.OracleDriver;

import java.sql.SQLException;

public class Main {
    public static void main(String[] args) throws Exception{
        OracleDriver.main(args);
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("UI effect failed: " + ex.getMessage());
        }

        try {
            System.out.println("Connecting to database...");
            DBConnection.connect();
            System.out.println("Connected successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Failed to connect to database:\n" + e.getMessage(),
                    "Startup Error",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1); // Stop app if DB is down
        }

        // Thread problem solution if the db is stuck
        SwingUtilities.invokeLater(() -> {
            new InitialWindow().setVisible(true);
        });

    }
}