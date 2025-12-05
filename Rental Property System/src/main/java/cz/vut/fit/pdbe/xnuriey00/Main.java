package cz.vut.fit.pdbe.xnuriey00;

import javax.swing.*;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import cz.vut.fit.pdbe.xnuriey00.ui.*;
import cz.vut.fit.pdbe.xnuriey00.util.*;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) throws Exception{
        //FlatIntelliJLaf.setup();
        UIManager.setLookAndFeel(new NimbusLookAndFeel());
        try {
            DBConnection.connect();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,"Failed to connect to database:\n" + e.getMessage(),"Startup Error",JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        SwingUtilities.invokeLater(Main::launch);
    }
    public static void launch() {
        new InitialWindow().setVisible(true);
    }
}