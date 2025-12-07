package cz.vut.fit.pdbe.xnuriey00.ui.components;

import cz.vut.fit.pdbe.xnuriey00.model.Property;
import cz.vut.fit.pdbe.xnuriey00.model.PropertyImage;
import cz.vut.fit.pdbe.xnuriey00.model.SpatialEntity;
import cz.vut.fit.pdbe.xnuriey00.queries.PropertyImageQueries;
import cz.vut.fit.pdbe.xnuriey00.util.DBConnection;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.sql.Connection;
import java.util.List;

/**
 * Panel for displaying and managing property images.
 */
public class PropertyImagePanel extends JPanel {

      private JLabel imageLabel;
      private JPanel imgContainer, imgNav, imgManip;
      private JButton prevImgBtn, nextImgBtn, addImgBtn, deleteImgBtn;
      private JButton mirrorImgBtn, rotateImgBtn, flipImgBtn;

      private List<PropertyImage> currentImages;
      private int currentImageIndex = 0;
      private SpatialEntity currentEntity;

      public PropertyImagePanel() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            initComponents();
      }

      private void initComponents() {
            // Image Container
            imgContainer = new JPanel(new BorderLayout());
            imgContainer.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            imgContainer.setPreferredSize(new Dimension(280, 180)); // Fixed aspect ratio
            imageLabel = new JLabel("No Image", SwingConstants.CENTER);
            imgContainer.add(imageLabel, BorderLayout.CENTER);

            // Image Controls
            imgNav = new JPanel(new GridLayout(1, 4, 2, 0));
            prevImgBtn = new JButton("⏪");
            addImgBtn = new JButton("➕");
            deleteImgBtn = new JButton("🗑");
            nextImgBtn = new JButton("⏩");

            // Compact formatting for controls
            for (JButton b : new JButton[] { prevImgBtn, addImgBtn, deleteImgBtn, nextImgBtn }) {
                  b.setMargin(new Insets(2, 2, 2, 2)); // Minimal padding
                  imgNav.add(b);
            }

            prevImgBtn.addActionListener(e -> navigateImage(-1));
            nextImgBtn.addActionListener(e -> navigateImage(1));
            addImgBtn.addActionListener(e -> addImage());
            deleteImgBtn.addActionListener(e -> deleteImage());

            // Image Manipulation Controls
            imgManip = new JPanel(new GridLayout(1, 3, 2, 0));
            imgManip.setBorder(BorderFactory.createTitledBorder("Edit Image"));

            mirrorImgBtn = new JButton("Mirror");
            rotateImgBtn = new JButton("Rotate");
            flipImgBtn = new JButton("Flip");

            for (JButton b : new JButton[] { mirrorImgBtn, rotateImgBtn, flipImgBtn }) {
                  b.setMargin(new Insets(2, 2, 2, 2));
                  b.setFont(new Font(null, Font.PLAIN, 10)); // Smaller font
                  imgManip.add(b);
            }

            mirrorImgBtn.addActionListener(e -> mirrorImage());
            rotateImgBtn.addActionListener(e -> rotateImage());
            flipImgBtn.addActionListener(e -> flipImage());

            add(imgContainer);
            add(imgNav);
            add(imgManip);
      }

      public void setCurrentEntity(SpatialEntity entity) {
            this.currentEntity = entity;
            if (entity instanceof Property) {
                  loadImages(((Property) entity).getPropertyId());
                  setVisible(true);
            } else {
                  clearImages();
                  setVisible(false);
            }
      }

      public void clearImages() {
            imageLabel.setIcon(null);
            imageLabel.setText("No Images");
            currentImages = null;
            currentImageIndex = 0;
      }

      private void loadImages(Long propertyId) {
            try (Connection conn = DBConnection.getConnection()) {
                  PropertyImageQueries imgQueries = new PropertyImageQueries(conn);
                  currentImages = imgQueries.getImagesForProperty(propertyId);
                  currentImageIndex = 0;
                  displayCurrentImage();
            } catch (Exception e) {
                  e.printStackTrace();
                  imageLabel.setText("Error loading images");
            }
      }

      private void displayCurrentImage() {
            if (currentImages == null || currentImages.isEmpty()) {
                  imageLabel.setIcon(null);
                  imageLabel.setText("No Images");
                  return;
            }

            if (currentImageIndex < 0)
                  currentImageIndex = currentImages.size() - 1;
            if (currentImageIndex >= currentImages.size())
                  currentImageIndex = 0;

            PropertyImage img = currentImages.get(currentImageIndex);
            imageLabel.setText("Loading...");

            try (Connection conn = DBConnection.getConnection()) {
                  PropertyImageQueries imgQueries = new PropertyImageQueries(conn);
                  byte[] data = imgQueries.getImageData(img.getImageId());
                  if (data != null) {
                        ImageIcon icon = new ImageIcon(data);
                        // Scale image
                        Image scaled = icon.getImage().getScaledInstance(280, 200, Image.SCALE_SMOOTH);
                        imageLabel.setIcon(new ImageIcon(scaled));
                        imageLabel.setText("");
                  } else {
                        imageLabel.setIcon(null);
                        imageLabel.setText("Image Data corrupted");
                  }
            } catch (Exception e) {
                  e.printStackTrace();
                  imageLabel.setText("Database Error" + e.getMessage());
            }
      }

      private void navigateImage(int direction) {
            if (currentImages == null || currentImages.isEmpty())
                  return;
            currentImageIndex += direction;
            displayCurrentImage();
      }

      private void addImage() {
            if (!(currentEntity instanceof Property)) {
                  JOptionPane.showMessageDialog(this, "Select a property first.");
                  return;
            }

            Property prop = (Property) currentEntity;

            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                  File file = chooser.getSelectedFile();
                  try (Connection conn = DBConnection.getConnection()) {
                        PropertyImageQueries imgQueries = new PropertyImageQueries(conn);
                        imgQueries.uploadImage(prop.getPropertyId(), file.getName(), file.getAbsolutePath());
                        JOptionPane.showMessageDialog(this, "Image Uploaded!");
                        loadImages(prop.getPropertyId());
                  } catch (Exception e) {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(this, "Upload Failed: " + e.getMessage());
                  }
            }
      }

      private void deleteImage() {
            if (currentImages == null || currentImages.isEmpty())
                  return;

            if (!(currentEntity instanceof Property)) {
                  return;
            }

            Property prop = (Property) currentEntity;

            int confirm = JOptionPane.showConfirmDialog(this, "Delete?");
            if (confirm == JOptionPane.YES_OPTION) {
                  PropertyImage img = currentImages.get(currentImageIndex);
                  try (Connection conn = DBConnection.getConnection()) {
                        PropertyImageQueries imgQueries = new PropertyImageQueries(conn);
                        imgQueries.deleteImage(img.getImageId());
                        JOptionPane.showMessageDialog(this, "Image Deleted.");
                        loadImages(prop.getPropertyId());
                  } catch (Exception e) {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(this, "Delete Failed: " + e.getMessage());
                  }
            }
      }

      private void mirrorImage() {
            processImageAction("mirror");
      }

      private void rotateImage() {
            processImageAction("rotate");
      }

      private void flipImage() {
            processImageAction("flip");
      }

      private void processImageAction(String action) {
            if (currentImages == null || currentImages.isEmpty()) {
                  JOptionPane.showMessageDialog(this, "No image to edit.");
                  return;
            }

            PropertyImage currentImg = currentImages.get(currentImageIndex);
            try (Connection conn = DBConnection.getConnection()) {
                  PropertyImageQueries imageQueries = new PropertyImageQueries(conn);

                  switch (action) {
                        case "rotate":
                              imageQueries.rotateImage(currentImg.getImageId());
                              break;
                        case "mirror":
                              imageQueries.mirrorImage(currentImg.getImageId());
                              break;
                        case "flip":
                              imageQueries.flipImage(currentImg.getImageId());
                              break;
                  }
                  // Refresh image
                  displayCurrentImage();
            } catch (Exception e) {
                  e.printStackTrace();
                  JOptionPane.showMessageDialog(this, "Error processing image: " + e.getMessage());
            }
      }
}
