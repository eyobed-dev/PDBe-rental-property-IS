package cz.vut.fit.pdbe.xnuriey00.ui.map.tools;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.*;

public class DrawSettingsPanel extends JPanel {

      private JComboBox<GeometryType> typeCombo;
      private JCheckBox holeCheckBox;
      private JButton finishBtn;
      private Runnable onFinishCallback;

      public DrawSettingsPanel() {
            setLayout(new FlowLayout(FlowLayout.LEFT));
            setBorder(BorderFactory.createTitledBorder("Draw Settings"));

            // Type Selection
            add(new JLabel("Type:"));
            typeCombo = new JComboBox<>(GeometryType.values());
            typeCombo.setSelectedItem(GeometryType.POLYGON);
            add(typeCombo);

            // Hole Checkbox
            holeCheckBox = new JCheckBox("Is Hole");
            holeCheckBox.setEnabled(true); // Default for Polygon
            add(holeCheckBox);

            // Finish Button (for Multi-geometries)
            finishBtn = new JButton("Finish Geometry");
            finishBtn.setBackground(new Color(200, 255, 200));
            finishBtn.addActionListener(e -> {
                  if (onFinishCallback != null) {
                        onFinishCallback.run();
                  }
            });
            add(finishBtn);

            // Listeners to update UI state
            typeCombo.addActionListener(e -> updateUIState());
            updateUIState();
      }

      private void updateUIState() {
            GeometryType type = (GeometryType) typeCombo.getSelectedItem();
            boolean isPoly = type == GeometryType.POLYGON || type == GeometryType.MULTI_POLYGON;
            holeCheckBox.setEnabled(isPoly);
            if (!isPoly) {
                  holeCheckBox.setSelected(false);
            }
      }

      public GeometryType getSelectedType() {
            return (GeometryType) typeCombo.getSelectedItem();
      }

      public boolean isHole() {
            return holeCheckBox.isSelected();
      }

      public void setOnFinishCallback(Runnable callback) {
            this.onFinishCallback = callback;
      }

      public void addTypeChangeListener(ActionListener l) {
            typeCombo.addActionListener(l);
      }
}
