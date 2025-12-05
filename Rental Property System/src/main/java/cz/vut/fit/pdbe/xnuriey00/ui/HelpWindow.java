package cz.vut.fit.pdbe.xnuriey00.ui;

import javax.swing.*;
import java.awt.*;

/**
 * A panel displaying the application help instructions.
 * 
 * @author eyobed-dev
 */
public class HelpWindow extends JFrame {

      public HelpWindow() {
            setLayout(new BorderLayout());
            setVisible(true);
            setSize(500, 400);
            setLocationRelativeTo(null);

            String helpText = "Rental Property Information System Help\n\n" +
                        "1. Searching Properties:\n" +
                        "   - Use the 'Search' bar to filter by name or address.\n" +
                        "   - Adjust the 'Price Limit' slider to filter by maximum price.\n" +
                        "   - Click 'Reset Filters' to clear all criteria.\n\n" +
                        "2. Map Features:\n" +
                        "   - Click 'Draw Area' to define a search polygon on the map.\n" +
                        "   - Use the navigation tools to pan and zoom.\n\n" +
                        "3. Multimedia Search:\n" +
                        "   - Select an image from a property.\n" +
                        "   - Enter weights (0.0 - 1.0) for Average Color (AC), Color Histogram (CH),\n" +
                        "     Positional Color (PC), and Texture (TX).\n" +
                        "   - Click 'Search Similar' to find visually similar images.\n\n" +
                        "4. Managing Properties:\n" +
                        "   - Select a property from the list to view details.\n" +
                        "   - Use 'Edit', 'Save', and 'Delete' buttons to manage records.\n" +
                        "   - Click 'Create New' to add a new property.\n\n" +
                        "5. Image Management:\n" +
                        "   - Use 'Upload Image' to add photos to a property.\n" +
                        "   - Use the carousel controls to view, rotate, mirror, or flip images.";

            JTextArea textArea = new JTextArea(helpText);
            textArea.setEditable(false);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            textArea.setFont(new Font("SansSerif", Font.PLAIN, 14));
            textArea.setMargin(new Insets(10, 10, 10, 10));

            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(500, 400));

            add(scrollPane, BorderLayout.CENTER);
      }
}
