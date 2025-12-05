package cz.vut.fit.pdbe.xnuriey00.ui.map;

import cz.vut.fit.pdbe.xnuriey00.ui.map.layers.MapLayer;

import javax.swing.*;
import java.awt.*;

//control panel for the map just below the map
public class MapControlPanel extends JPanel {

      public MapControlPanel(MapPanel mapPanel) {
            setLayout(new FlowLayout(FlowLayout.LEFT));
            setBorder(BorderFactory.createTitledBorder("Map Controls"));

            // Layer Toggles
            JPanel layerPanel = new JPanel(new FlowLayout());
            layerPanel.setOpaque(false);
            layerPanel.add(new JLabel("Layers: "));

            // Add checkboxes for each layer
            for (MapLayer layer : mapPanel.getLayers()) {
                  JCheckBox cb = new JCheckBox(layer.getName());
                  cb.setSelected(layer.isVisible());
                  cb.setOpaque(false);
                  cb.addActionListener(e -> {
                        // toggle layer visibility then repaint the map
                        layer.setVisible(cb.isSelected());
                        mapPanel.repaint();
                  });
                  layerPanel.add(cb);
            }
            add(layerPanel);

      }
}
