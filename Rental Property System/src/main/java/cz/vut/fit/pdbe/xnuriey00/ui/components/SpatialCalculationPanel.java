package cz.vut.fit.pdbe.xnuriey00.ui.components;

import cz.vut.fit.pdbe.xnuriey00.model.SpatialEntity;
import cz.vut.fit.pdbe.xnuriey00.queries.PropertyQueries;
import cz.vut.fit.pdbe.xnuriey00.util.DBConnection;
import oracle.spatial.geometry.JGeometry;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.util.function.Supplier;

public class SpatialCalculationPanel extends JPanel {

      private final Supplier<SpatialEntity> entitySupplier;
      private JLabel areaLabel;
      private JLabel perimeterLabel;
      private JLabel centroidLabel;

      public SpatialCalculationPanel(Supplier<SpatialEntity> entitySupplier) {
            this.entitySupplier = entitySupplier;
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(BorderFactory.createTitledBorder("Spatial Calculations"));
            initComponents();
      }

      private void initComponents() {
            // Area
            JPanel areaPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JButton areaBtn = new JButton("Calculate Area");
            areaLabel = new JLabel("Result: -");
            areaBtn.addActionListener(e -> calculateArea());
            areaPanel.add(areaBtn);
            areaPanel.add(areaLabel);

            // Perimeter
            JPanel perimeterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JButton perimeterBtn = new JButton("Calculate Perimeter");
            perimeterLabel = new JLabel("Result: -");
            perimeterBtn.addActionListener(e -> calculatePerimeter());
            perimeterPanel.add(perimeterBtn);
            perimeterPanel.add(perimeterLabel);

            // Centroid
            JPanel centroidPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JButton centroidBtn = new JButton("Get Centroid");
            centroidLabel = new JLabel("Result: -");
            centroidBtn.addActionListener(e -> getCentroid());
            centroidPanel.add(centroidBtn);
            centroidPanel.add(centroidLabel);

            add(areaPanel);
            add(perimeterPanel);
            add(centroidPanel);
      }

      private void calculateArea() {
            SpatialEntity entity = entitySupplier.get();
            if (entity == null) {
                  JOptionPane.showMessageDialog(this, "No entity selected.");
                  return;
            }

            try (Connection conn = DBConnection.getConnection()) {
                  PropertyQueries queries = new PropertyQueries(conn);
                  double area = queries.calculateArea(entity.getId());
                  areaLabel.setText(String.format("Result: %.2f units²", area));
            } catch (Exception e) {
                  
                  areaLabel.setText("Result: Error");
                  JOptionPane.showMessageDialog(this, "Error calculating area: " + e.getMessage());
            }
      }

      private void calculatePerimeter() {
            SpatialEntity entity = entitySupplier.get();
            if (entity == null) {
                  JOptionPane.showMessageDialog(this, "No entity selected.");
                  return;
            }

            try (Connection conn = DBConnection.getConnection()) {
                  PropertyQueries queries = new PropertyQueries(conn);
                  double perimeter = queries.calculatePerimeter(entity.getId());
                  perimeterLabel.setText(String.format("Result: %.2f units", perimeter));
            } catch (Exception e) {
                  perimeterLabel.setText("Result: Error");
                  JOptionPane.showMessageDialog(this, "Error calculating perimeter: " + e.getMessage());
            }
      }

      private void getCentroid() {
            SpatialEntity entity = entitySupplier.get();
            if (entity == null) {
                  JOptionPane.showMessageDialog(this, "No entity selected.");
                  return;
            }

            try (Connection conn = DBConnection.getConnection()) {
                  PropertyQueries queries = new PropertyQueries(conn);
                  JGeometry centroid = queries.getCentroid(entity.getId());
                  if (centroid != null && centroid.getPoint() != null) {
                        double[] point = centroid.getPoint();
                        centroidLabel.setText(String.format("Result: [%.2f, %.2f]", point[0], point[1]));
                  } else {
                        centroidLabel.setText("Result: No centroid");
                  }
            } catch (Exception e) {
                  
                  centroidLabel.setText("Result: Error");
                  JOptionPane.showMessageDialog(this, "Error getting centroid: " + e.getMessage());
            }
      }
}
