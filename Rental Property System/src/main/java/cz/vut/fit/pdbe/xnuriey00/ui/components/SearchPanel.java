package cz.vut.fit.pdbe.xnuriey00.ui.components;

import cz.vut.fit.pdbe.xnuriey00.model.Property;
import cz.vut.fit.pdbe.xnuriey00.model.PropertyImage;
import cz.vut.fit.pdbe.xnuriey00.model.SpatialEntity;
import cz.vut.fit.pdbe.xnuriey00.queries.*;
import cz.vut.fit.pdbe.xnuriey00.ui.handlers.EntityHandler;
import cz.vut.fit.pdbe.xnuriey00.ui.map.MapPanel;
import cz.vut.fit.pdbe.xnuriey00.ui.map.tools.DrawTool;
import cz.vut.fit.pdbe.xnuriey00.ui.map.tools.NavigationTool;
import cz.vut.fit.pdbe.xnuriey00.ui.map.tools.DrawSettingsPanel;
import cz.vut.fit.pdbe.xnuriey00.util.DBConnection;
import oracle.spatial.geometry.JGeometry;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Consumer;

/**
 * Panel for search controls and results table.
 */
public class SearchPanel extends JPanel {

      private JTextField searchField;
      private JSlider priceSlider;
      private JSlider transitDistanceSlider;
      private JComboBox<String> spatialTargetCombo;
      private JTable table;
      private DefaultTableModel tableModel;

      private final MapPanel mapPanel;
      private final DrawSettingsPanel drawSettingsPanel;
      private final Consumer<SpatialEntity> onEntitySelected;
      private final Consumer<List<? extends SpatialEntity>> onViewUpdate;
      private final Runnable onLoadEverything;

      public SearchPanel(MapPanel mapPanel, DrawSettingsPanel drawSettingsPanel,
                  Consumer<SpatialEntity> onEntitySelected,
                  Consumer<List<? extends SpatialEntity>> onViewUpdate,
                  Runnable onLoadEverything) {
            this.mapPanel = mapPanel;
            this.drawSettingsPanel = drawSettingsPanel;
            this.onEntitySelected = onEntitySelected;
            this.onViewUpdate = onViewUpdate;
            this.onLoadEverything = onLoadEverything;

            setLayout(new BorderLayout(10, 10));
            setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            setPreferredSize(new Dimension(300, 0)); // Height will be handled by parent

            initComponents();
            initTableListener();
      }

      private void initComponents() {
            JPanel controlsWrapper = new JPanel();
            controlsWrapper.setLayout(new BoxLayout(controlsWrapper, BoxLayout.Y_AXIS));

            // Relational Search Panel
            JPanel relPanel = new JPanel(new GridLayout(0, 1, 5, 5));
            relPanel.setBorder(BorderFactory.createTitledBorder("Relational Search"));

            searchField = new JTextField("Search");
            searchField.addActionListener(e -> filterProperties());

            JLabel priceLabel = new JLabel("Price Limit: $5000");
            priceSlider = new JSlider(0, 5000, 5000);
            priceSlider.setMajorTickSpacing(1000);
            priceSlider.setPaintTicks(true);
            priceSlider.addChangeListener(e -> {
                  priceLabel.setText("Price Limit: $" + priceSlider.getValue());
                  if (!priceSlider.getValueIsAdjusting())
                        filterProperties();
            });

            relPanel.add(searchField);
            relPanel.add(priceLabel);
            relPanel.add(priceSlider);

            // Spatial Search Panel
            JPanel spatPanel = new JPanel();
            spatPanel.setLayout(new BoxLayout(spatPanel, BoxLayout.Y_AXIS));
            spatPanel.setBorder(BorderFactory.createTitledBorder("Spatial Search"));

            // Draw Area Button
            JButton drawBtn = new JButton("🗺️ Draw Area");
            drawBtn.setBackground(new Color(144, 229, 255));
            drawBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            drawBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
            drawBtn.addActionListener(e -> {
                  if (mapPanel != null) {
                        drawSettingsPanel.setVisible(true);
                        mapPanel.setCurrentTool(new DrawTool(drawSettingsPanel, this::filterPropertiesByGeometry));
                        JOptionPane.showMessageDialog(this, "Draw a polygon/circle on the map.");
                  }
            });

            // Spatial Target Combo
            JLabel targetLabel = new JLabel("Target Entity:");
            targetLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            String[] targets = { "Transit", "Schools", "Bike Routes" };
            spatialTargetCombo = new JComboBox<>(targets);
            spatialTargetCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

            // Distance Slider
            JLabel distanceLabel = new JLabel("Distance:500m");
            distanceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            transitDistanceSlider = new JSlider(100, 1000, 500);
            transitDistanceSlider.setMajorTickSpacing(100);
            transitDistanceSlider.setMinorTickSpacing(100);
            transitDistanceSlider.setPaintTicks(true);
            transitDistanceSlider.setSnapToTicks(true);
            transitDistanceSlider.addChangeListener(e -> {
                  int value = transitDistanceSlider.getValue();
                  distanceLabel.setText("Distance: " + value + "m");
            });

            // Find Nearby Button
            JButton findNearbyBtn = new JButton("📍 Find Nearby");
            findNearbyBtn.setBackground(new Color(144, 238, 144)); // Light green
            findNearbyBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            findNearbyBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
            findNearbyBtn.addActionListener(e -> filterPropertiesNearby());

            // Find Nearest Button
            JButton findNearestBtn = new JButton("🔍 Find Nearest Neighbors");
            findNearestBtn.setBackground(new Color(255, 255, 153)); // Light yellow
            findNearestBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            findNearestBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
            findNearestBtn.addActionListener(e -> findNearestEntity());

            spatPanel.add(drawBtn);
            spatPanel.add(Box.createVerticalStrut(10));
            spatPanel.add(targetLabel);
            spatPanel.add(spatialTargetCombo);
            spatPanel.add(Box.createVerticalStrut(5));
            spatPanel.add(distanceLabel);
            spatPanel.add(transitDistanceSlider);
            spatPanel.add(Box.createVerticalStrut(5));
            spatPanel.add(findNearbyBtn);
            spatPanel.add(Box.createVerticalStrut(5));
            spatPanel.add(findNearestBtn);

            // Multimedia Search Pane
            JPanel mediaPanel = new JPanel(new GridLayout(0, 2, 5, 5)); // 2 columns, auto rows
            mediaPanel.setBorder(BorderFactory.createTitledBorder("Multimedia Search"));

            // Weight inputs
            JTextField acWeightField = new JTextField("0.3");
            acWeightField.setBorder(BorderFactory.createTitledBorder("AC"));

            JTextField chWeightField = new JTextField("0.3");
            chWeightField.setBorder(BorderFactory.createTitledBorder("CH"));

            JTextField pcWeightField = new JTextField("0.3");
            pcWeightField.setBorder(BorderFactory.createTitledBorder("PC"));

            JTextField txWeightField = new JTextField("0.1");
            txWeightField.setBorder(BorderFactory.createTitledBorder("TX"));

            JButton searchImgBtn = new JButton("Search Similar");
            searchImgBtn.addActionListener(e -> searchSimilarImages(
                        acWeightField, chWeightField, pcWeightField, txWeightField));

            JButton uploadSearchBtn = new JButton("Upload & Search");
            uploadSearchBtn.addActionListener(e -> uploadAndSearchSimilar(
                        acWeightField, chWeightField, pcWeightField, txWeightField));

            mediaPanel.add(searchImgBtn);
            mediaPanel.add(uploadSearchBtn);
            mediaPanel.add(acWeightField);
            mediaPanel.add(chWeightField);
            mediaPanel.add(pcWeightField);
            mediaPanel.add(txWeightField);

            // Reset Button
            JButton resetBtn = new JButton("Reset Filters");
            resetBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
            resetBtn.addActionListener(e -> {
                  searchField.setText("Search");
                  priceSlider.setValue(5000);
                  transitDistanceSlider.setValue(500);
                  mapPanel.clearOverlays();
                  if (onLoadEverything != null)
                        onLoadEverything.run();
            });

            // Add groups to wrapper with spacing
            controlsWrapper.add(relPanel);
            controlsWrapper.add(Box.createVerticalStrut(10));
            controlsWrapper.add(spatPanel);
            controlsWrapper.add(Box.createVerticalStrut(10));
            controlsWrapper.add(mediaPanel);
            controlsWrapper.add(Box.createVerticalStrut(10));
            controlsWrapper.add(resetBtn);

            // Results Table
            String[] columns = { "ID", "Name" };
            tableModel = new DefaultTableModel(new Object[0][0], columns);
            table = new JTable(tableModel);

            add(controlsWrapper, BorderLayout.NORTH);
            add(new JScrollPane(table), BorderLayout.CENTER);
      }

      // Helper to update table
      public void updateTable(List<? extends SpatialEntity> entities, EntityHandler handler) {
            String[] columns = handler.getTableColumns();
            tableModel.setColumnIdentifiers(columns);
            tableModel.setRowCount(0);

            for (SpatialEntity entity : entities) {
                  Object[] rowData = handler.getTableRowData(entity);
                  tableModel.addRow(rowData);
            }

            // we can store the list here.
            // Let's store the list here for selection mapping.
            this.currentEntities = entities;
      }

      private List<? extends SpatialEntity> currentEntities;

      // Re-init table listener properly
      private void initTableListener() {
            table.getSelectionModel().addListSelectionListener(event -> {
                  if (!event.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                        Long id = (Long) tableModel.getValueAt(table.getSelectedRow(), 0);
                        if (currentEntities != null) {
                              for (SpatialEntity entity : currentEntities) {
                                    if (entity.getId().equals(id)) {
                                          if (onEntitySelected != null) {
                                                onEntitySelected.accept(entity);
                                          }
                                          break;
                                    }
                              }
                        }
                  }
            });
      }

      private void filterProperties() {
            try {
                  Connection conn = DBConnection.getConnection();
                  PropertyQueries queries = new PropertyQueries(conn);

                  String searchText = searchField.getText();
                  if (searchText.equals("Search")) {
                        searchText = "";
                  }

                  double maxPrice = priceSlider.getValue();

                  List<Property> properties = queries.filterProperties(searchText, maxPrice);
                  if (onViewUpdate != null)
                        onViewUpdate.accept(properties);
            } catch (SQLException e) {

                  JOptionPane.showMessageDialog(this, "Error filtering properties: " + e.getMessage());
            }
      }

      private void filterPropertiesByGeometry(JGeometry geometry) {
            drawSettingsPanel.setVisible(false);

            try (Connection conn = DBConnection.getConnection()) {
                  PropertyQueries queries = new PropertyQueries(conn);
                  List<Property> props = queries.getPropertiesIntersecting(geometry);
                  if (onViewUpdate != null)
                        onViewUpdate.accept(props);
                  JOptionPane.showMessageDialog(this, "Found " + props.size() + " properties in area.");
            } catch (Exception ex) {

                  JOptionPane.showMessageDialog(this, "Error searching area: " + ex.getMessage());
            }

            mapPanel.clearOverlays();
            mapPanel.addOverlay(geometry, new Color(255, 0, 0, 50), "Search Area");
            mapPanel.setCurrentTool(new NavigationTool());
      }

      private void filterPropertiesNearby() {
            double distance = transitDistanceSlider.getValue();
            String target = (String) spatialTargetCombo.getSelectedItem();

            try (Connection conn = DBConnection.getConnection()) {
                  PropertyQueries queries = new PropertyQueries(conn);
                  List<Property> props = new java.util.ArrayList<>();

                  if ("Transit".equals(target)) {
                        props = queries.findPropertiesNearTransit(distance);
                  } else if ("Schools".equals(target)) {
                        props = queries.findPropertiesNearSchools(distance);
                  } else if ("Bike Routes".equals(target)) {
                        props = queries.findPropertiesNearBikeRoutes(distance);
                  }

                  if (onViewUpdate != null)
                        onViewUpdate.accept(props);

                  String message = "Found " + props.size() + " properties within " +
                              (int) distance + "m of " + target + ".";
                  JOptionPane.showMessageDialog(this, message);
            } catch (Exception ex) {
                  JOptionPane.showMessageDialog(this, "Error searching nearby: " + ex.getMessage());
            }

            mapPanel.clearOverlays();
            mapPanel.setCurrentTool(new NavigationTool());
      }

      private void findNearestEntity() {
            if (table.getSelectedRow() == -1) {
                  JOptionPane.showMessageDialog(this,
                              "Please select a property from the table first.\n\n",
                              "No Property Selected",
                              JOptionPane.INFORMATION_MESSAGE);
                  return;
            }

            Long propertyId = (Long) tableModel.getValueAt(table.getSelectedRow(), 0);
            String propertyName = (String) tableModel.getValueAt(table.getSelectedRow(), 1);
            String target = (String) spatialTargetCombo.getSelectedItem();

            try (Connection conn = DBConnection.getConnection()) {
                  PropertyQueries propQueries = new PropertyQueries(conn);
                  Property prop = (Property) propQueries.getById(propertyId);

                  SpatialEntity nearest = null;
                  String entityType = "";

                  if ("Transit".equals(target)) {
                        nearest = new TransitQueries(conn)
                                    .getNearest(prop.getGeometry());
                        entityType = "Transit Stop";
                  } else if ("Schools".equals(target)) {
                        nearest = new SchoolQueries(conn)
                                    .getNearest(prop.getGeometry());
                        entityType = "School";
                  } else if ("Bike Routes".equals(target)) {
                        nearest = new BikeRouteQueries(conn)
                                    .getNearest(prop.getGeometry());
                        entityType = "Bike Route";
                  }

                  if (nearest != null) {
                        mapPanel.clearOverlays();

                        // Highlight both the property and the nearest entity
                        mapPanel.addOverlay(prop.getGeometry(), new Color(0, 255, 0, 100), "Selected: " + propertyName);
                        mapPanel.addOverlay(nearest.getGeometry(), new Color(255, 0, 255, 150),
                                    "Nearest " + entityType);

                        String message = String.format(
                                    """
                                                Found nearest %s to '%s':

                                                📍 %s: %s

                                                Both highlighted on the map:
                                                Green > Selected property
                                                Purple > Nearest %s""",
                                    entityType, propertyName,
                                    entityType, nearest.getName(),
                                    entityType.toLowerCase());

                        JOptionPane.showMessageDialog(this, message,
                                    "Nearest " + entityType + " Found",
                                    JOptionPane.INFORMATION_MESSAGE);
                  } else {
                        JOptionPane.showMessageDialog(this,
                                    "No " + target + " found in the database.",
                                    "Not Found",
                                    JOptionPane.WARNING_MESSAGE);
                  }

            } catch (Exception ex) {

                  JOptionPane.showMessageDialog(this, "Error finding nearest: " + ex.getMessage());
            }
      }

      public JTable getTable() {
            return table;
      }

      public DefaultTableModel getTableModel() {
            return tableModel;
      }

      /**
       * Searches for images similar to the currently selected property image.
       */
      private void searchSimilarImages(JTextField acField, JTextField chField,
                  JTextField pcField, JTextField txField) {
            if (table.getSelectedRow() == -1) {
                  JOptionPane.showMessageDialog(this,
                              "Please select a property from the table first.",
                              "No Property Selected",
                              JOptionPane.INFORMATION_MESSAGE);
                  return;
            }

            Long propertyId = (Long) tableModel.getValueAt(table.getSelectedRow(), 0);

            try {
                  // Parse weights
                  double acWeight = Double.parseDouble(acField.getText());
                  double chWeight = Double.parseDouble(chField.getText());
                  double pcWeight = Double.parseDouble(pcField.getText());
                  double txWeight = Double.parseDouble(txField.getText());

                  try (Connection conn = DBConnection.getConnection()) {
                        PropertyImageQueries imgQueries = new PropertyImageQueries(conn);

                        // Get images for the selected property
                        List<PropertyImage> propertyImages = imgQueries.getImagesForProperty(propertyId);

                        if (propertyImages.isEmpty()) {
                              JOptionPane.showMessageDialog(this,
                                          "Selected property has no images.",
                                          "No Images",
                                          JOptionPane.WARNING_MESSAGE);
                              return;
                        }

                        // Use the first image as the source
                        Long sourceImageId = propertyImages.get(0).getImageId();

                        // Find similar images
                        List<PropertyImage> similarImages = imgQueries.findSimilarImages(
                                    sourceImageId, acWeight, chWeight, pcWeight, txWeight);

                        if (similarImages.isEmpty()) {
                              JOptionPane.showMessageDialog(this,
                                          "No similar images found.",
                                          "No Results",
                                          JOptionPane.INFORMATION_MESSAGE);
                              return;
                        }

                        // Display results in a new window
                        displaySimilarImagesWindow(similarImages, imgQueries);
                  }
            } catch (NumberFormatException ex) {
                  JOptionPane.showMessageDialog(this,
                              "Invalid weight values. Please enter numbers between 0.0 and 1.0.",
                              "Invalid Input",
                              JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                  JOptionPane.showMessageDialog(this,
                              "Error searching for similar images: " + ex.getMessage(),
                              "Error",
                              JOptionPane.ERROR_MESSAGE);
            }
      }

      /**
       * Displays similar images in a closable JFrame with grid layout.
       */
      private void displaySimilarImagesWindow(List<PropertyImage> images, PropertyImageQueries imgQueries) {
            JFrame resultsFrame = new JFrame("Similar Images");
            resultsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            resultsFrame.setSize(800, 600);

            JPanel gridPanel = new JPanel(new GridLayout(0, 3, 10, 10)); // 3 columns
            gridPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            for (PropertyImage img : images) {
                  try {
                        byte[] imageData = imgQueries.getImageData(img.getImageId());
                        ImageIcon icon = new ImageIcon(imageData);

                        // Scale image to fit
                        Image scaledImage = icon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
                        JLabel imageLabel = new JLabel(new ImageIcon(scaledImage));
                        imageLabel.setBorder(BorderFactory.createTitledBorder(img.getImageName()));

                        gridPanel.add(imageLabel);
                  } catch (Exception ex) {
                        JLabel errorLabel = new JLabel("Error loading image");
                        errorLabel.setBorder(BorderFactory.createTitledBorder(img.getImageName()));
                        gridPanel.add(errorLabel);
                  }
            }

            JScrollPane scrollPane = new JScrollPane(gridPanel);
            resultsFrame.add(scrollPane);
            resultsFrame.setLocationRelativeTo(this);
            resultsFrame.setVisible(true);
      }

      /**
       * Uploads an image file and searches for similar images in the database.
       */
      private void uploadAndSearchSimilar(JTextField acField, JTextField chField,
                  JTextField pcField, JTextField txField) {
            try {
                  // Parse weights
                  double acWeight = Double.parseDouble(acField.getText());
                  double chWeight = Double.parseDouble(chField.getText());
                  double pcWeight = Double.parseDouble(pcField.getText());
                  double txWeight = Double.parseDouble(txField.getText());

                  // File chooser for image selection
                  JFileChooser fileChooser = new JFileChooser();
                  fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                              "Image Files", "jpg", "jpeg", "png", "gif", "bmp"));

                  int result = fileChooser.showOpenDialog(this);
                  if (result != JFileChooser.APPROVE_OPTION) {
                        return; // User cancelled
                  }

                  String filePath = fileChooser.getSelectedFile().getAbsolutePath();

                  try (Connection conn = DBConnection.getConnection()) {
                        PropertyImageQueries imgQueries = new PropertyImageQueries(conn);

                        // Upload image temporarily (using property ID -1 as a temp marker)
                        imgQueries.uploadImage(-1L, "temp_search_image", filePath);

                        // Get the uploaded temp image ID by querying for it
                        List<PropertyImage> tempImages = imgQueries.getImagesForProperty(-1L);
                        if (tempImages.isEmpty()) {
                              JOptionPane.showMessageDialog(this,
                                          "Failed to upload temporary image.",
                                          "Upload Error",
                                          JOptionPane.ERROR_MESSAGE);
                              return;
                        }

                        Long tempImageId = tempImages.get(0).getImageId();

                        try {
                              // Find similar images
                              List<PropertyImage> similarImages = imgQueries.findSimilarImages(
                                          tempImageId, acWeight, chWeight, pcWeight, txWeight);

                              if (similarImages.isEmpty()) {
                                    JOptionPane.showMessageDialog(this,
                                                "No similar images found.",
                                                "No Results",
                                                JOptionPane.INFORMATION_MESSAGE);
                              } else {
                                    // Display results
                                    displaySimilarImagesWindow(similarImages, imgQueries);
                              }
                        } finally {
                              // Clean up: delete the temporary image
                              try {
                                    imgQueries.deleteImage(tempImageId);
                              } catch (Exception cleanupEx) {
                                    // Ignore cleanup errors
                              }
                        }
                  }
            } catch (NumberFormatException ex) {
                  JOptionPane.showMessageDialog(this,
                              "Invalid weight values. Please enter numbers between 0.0 and 1.0.",
                              "Invalid Input",
                              JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                  JOptionPane.showMessageDialog(this,
                              "Error searching for similar images: " + ex.getMessage(),
                              "Error",
                              JOptionPane.ERROR_MESSAGE);
            }
      }
}
