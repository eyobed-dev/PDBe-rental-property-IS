package cz.vut.fit.pdbe.xnuriey00.ui;

import cz.vut.fit.pdbe.xnuriey00.Main;
import cz.vut.fit.pdbe.xnuriey00.model.*;
import cz.vut.fit.pdbe.xnuriey00.queries.*;
import cz.vut.fit.pdbe.xnuriey00.ui.map.MapControlPanel;
import cz.vut.fit.pdbe.xnuriey00.ui.map.MapPanel;
import cz.vut.fit.pdbe.xnuriey00.ui.map.tools.DrawSettingsPanel;
import cz.vut.fit.pdbe.xnuriey00.ui.map.tools.DrawTool;
import cz.vut.fit.pdbe.xnuriey00.ui.map.tools.NavigationTool;
import cz.vut.fit.pdbe.xnuriey00.util.DBConnection;
import cz.vut.fit.pdbe.xnuriey00.util.UserSession;
import oracle.spatial.geometry.JGeometry;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * The main application window
 */
public class MainWindow extends JFrame {

    private JTextField searchField; // Text field for keyword searching properties.
    private JSlider priceSlider; // Slider to filter properties by maximum price.
    private JTable table; // Table to display search results.
    private DefaultTableModel tableModel; // The data model backing the results table.

    private MapPanel mapPanel; // The custom panel responsible for rendering the map and handling spatial
                               // tools. Inside ./map

    // Right Panel Components
    private JTextField nameField, addressField, priceField, bedsField, areaField;
    private JTextArea descArea;
    private JButton editBtn, deleteBtn, saveBtn, cancelBtn, createBtn, redrawBtn;

    // Image Carousel Components
    private JLabel imageLabel;
    private JButton prevImgBtn, nextImgBtn, addImgBtn, deleteImgBtn;
    private List<PropertyImage> currentImages;
    private int currentImageIndex = 0;

    // Draw Settings
    private DrawSettingsPanel drawSettingsPanel;

    // Cache of all currently loaded properties to avoid constant DB fetching for
    // details.
    private List<Property> allProperties;

    // The property currently selected or being edited.
    private Property currentProperty;

    // Temporary storage for geometry drawn on the map during property creation.
    private JGeometry tempGeometry;
    // Flag indicating if the user is currently in the "Create New Property" flow.

    private boolean isCreating = false;
    // CONSTRUCTOR

    public MainWindow() {
        setTitle("Rental Property IS");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Start full screen
        setLayout(new BorderLayout(10, 10)); // 10px gap
        setBackground(Color.LIGHT_GRAY);

        // Warning : DB connections must be closed when the application exits.
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    DBConnection.close();
                } catch (SQLException sqlException) {
                    sqlException.printStackTrace();
                }
            }
        });

        // ============================== Header Content ==============================
        add(createHeader(), BorderLayout.NORTH);

        // ============================== Main Content ===============================
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));

        // Initialize the three main panels
        contentPanel.add(createLeftControlPanel(), BorderLayout.WEST);
        contentPanel.add(createCenterMapPanel(), BorderLayout.CENTER);
        contentPanel.add(createRightDetailPanel(), BorderLayout.EAST);

        add(contentPanel, BorderLayout.CENTER);

        // Center the window on screen
        setLocationRelativeTo(null);

        // Initial Data Load: Fetches properties, schools, transit, etc.
        loadEverything();
    }

    /**
     * Creates the top header panel containing the logo, title, and system buttons.
     * 
     * @return A constructed JPanel for the header.
     */
    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEtchedBorder());

        // Title and Logo
        JLabel title = new JLabel("RENTAL PROPERTY IS");
        title.setFont(new Font(null, Font.BOLD, 28));
        title.setIcon(new ImageIcon(getClass().getClassLoader().getResource("logo.png")));

        // Button Container
        JPanel buttonPanel = new JPanel(new FlowLayout());

        JButton refreshBtn = new JButton("🔄 REFRESH");
        JButton helpBtn = new JButton("❓ HELP");
        JButton logoutBtn = new JButton("⏻ LOGOUT");

        // Action Listeners
        refreshBtn.addActionListener(e -> System.out.println("Refresh clicked"));
        helpBtn.addActionListener(e -> System.out.println("Help clicked"));

        // Logout Logic: Clear session, close window, and relaunch Main login
        logoutBtn.addActionListener(e -> {
            UserSession.getInstance().logout();
            this.dispose();
            Main.launch();
        });

        // Admin-only feature check
        if (UserSession.getInstance().isAdmin()) {
            JButton manageUsers = new JButton("\uD83D\uDC65 MANAGE USERS");
            manageUsers.addActionListener(e -> new UserManagementWindow().setVisible(true));
            buttonPanel.add(manageUsers);
        }

        buttonPanel.add(refreshBtn);
        buttonPanel.add(helpBtn);
        buttonPanel.add(logoutBtn);

        header.add(title, BorderLayout.WEST);
        header.add(buttonPanel, BorderLayout.EAST);
        return header;
    }

    /**
     * Creates the left-side control panel containing search tools and results list.
     * 
     * @return A constructed JPanel for the left sidebar.
     */
    private JPanel createLeftControlPanel() {
        // Main Layout
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setPreferredSize(new Dimension(300, getHeight()));

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

        // spatial Search Panel
        JPanel spatPanel = new JPanel(new GridLayout(1, 1)); // Single cell grid for full expansion
        spatPanel.setBorder(BorderFactory.createTitledBorder("Spatial Search"));

        JButton drawBtn = new JButton("\uD83D\uDDFA\uFE0F Draw Area");
        drawBtn.setBackground(new Color(144, 229, 255));
        drawBtn.addActionListener(e -> {
            if (mapPanel != null) {
                drawSettingsPanel.setVisible(true);
                mapPanel.setCurrentTool(new DrawTool(drawSettingsPanel, this::filterPropertiesByGeometry));
                JOptionPane.showMessageDialog(this, "Draw a polygon/circle on the map.");
            }
        });
        spatPanel.add(drawBtn);

        // Multimedia Search Pane
        JPanel mediaPanel = new JPanel(new GridLayout(1, 1));
        mediaPanel.setBorder(BorderFactory.createTitledBorder("Multimedia Search"));

        JButton imgBtn = new JButton("\uD83D\uDDBC\uFE0F Upload Image");
        imgBtn.setBackground(new Color(251, 207, 207));
        imgBtn.addActionListener(e -> System.out.println("Action: Upload Image"));
        mediaPanel.add(imgBtn);

        // Reset Button
        JButton resetBtn = new JButton("Reset Filters");
        // Fix width to match panels using setMaximumSize in BoxLayout
        resetBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        resetBtn.addActionListener(e -> {
            searchField.setText("Search");
            priceSlider.setValue(5000);
            mapPanel.clearOverlays();
            clearFields();
            loadEverything();
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
        String[] columns = { "ID", "Name", "Address", "Price" };
        tableModel = new DefaultTableModel(new Object[0][0], columns);
        table = new JTable(tableModel);
        table.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                Long id = (Long) tableModel.getValueAt(table.getSelectedRow(), 0);
                if (allProperties != null) {
                    allProperties.stream()
                            .filter(p -> p.getPropertyId().equals(id))
                            .findFirst()
                            .ifPresent(this::populateDetails);
                }
            }
        });

        // finish
        panel.add(controlsWrapper, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        return panel;
    }

    /**
     * Creates the center panel containing the map and its controls.
     * 
     * @return A constructed JPanel for the map.
     */
    private JPanel createCenterMapPanel() {
        mapPanel = new MapPanel();
        JPanel mapContainer = new JPanel(new BorderLayout());
        mapContainer.add(mapPanel, BorderLayout.CENTER);

        // Add standard map controls and settings at the bottom
        JPanel bottomPanel = new JPanel(new BorderLayout());

        MapControlPanel controlPanel = new MapControlPanel(mapPanel);
        bottomPanel.add(controlPanel, BorderLayout.EAST);

        // Draw Settings (Initially Hidden)
        drawSettingsPanel = new DrawSettingsPanel();
        drawSettingsPanel.setVisible(false);
        bottomPanel.add(drawSettingsPanel, BorderLayout.WEST);

        mapContainer.add(bottomPanel, BorderLayout.SOUTH);

        return mapContainer;
    }

    /**
     * Creates the right-side details panel for viewing and editing properties.
     * 
     * @return A constructed JPanel for details.
     */
    private JPanel createRightDetailPanel() {
    //Main Container
    JPanel panel = new JPanel(new BorderLayout(0, 15));
    panel.setPreferredSize(new Dimension(320, 0));
    panel.setBorder(new EmptyBorder(15, 15, 15, 15));

    //Top Section
    JPanel topSection = new JPanel();
    topSection.setLayout(new BoxLayout(topSection, BoxLayout.Y_AXIS));
    
    createBtn = new JButton("➕ Create New Property");
    createBtn.setBackground(new Color(144, 238, 144));
    createBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
    createBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35)); // Full width
    createBtn.addActionListener(e -> initiateCreateFlow());

    // Image Container
    JPanel imgContainer = new JPanel(new BorderLayout());
    imgContainer.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
    imgContainer.setPreferredSize(new Dimension(280, 180)); // Fixed aspect ratio
    imageLabel = new JLabel("No Image", SwingConstants.CENTER);
    imgContainer.add(imageLabel, BorderLayout.CENTER);

    // Image Controls
    JPanel imgNav = new JPanel(new GridLayout(1, 4, 2, 0));
    prevImgBtn = new JButton("◀");
    addImgBtn = new JButton("➕");
    deleteImgBtn = new JButton("🗑");
    nextImgBtn = new JButton("▶");
    
    // Compact formatting for controls
    for (JButton b : new JButton[]{prevImgBtn, addImgBtn, deleteImgBtn, nextImgBtn}) {
        b.setMargin(new Insets(2, 2, 2, 2)); // Minimal padding
        imgNav.add(b);
    }

    prevImgBtn.addActionListener(e -> navigateImage(-1));
    nextImgBtn.addActionListener(e -> navigateImage(1));
    addImgBtn.addActionListener(e -> addImage());
    deleteImgBtn.addActionListener(e -> deleteImage());

    topSection.add(createBtn);
    topSection.add(Box.createVerticalStrut(10));
    topSection.add(imgContainer);
    topSection.add(imgNav); // Nav bar sits flush under image

    //Center Section: Data Form
    JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 10)); // 2 Cols, 10px gaps
    
    // Inline field initialization 
    nameField = new JTextField();
    addressField = new JTextField();
    priceField = new JTextField();
    bedsField = new JTextField();
    areaField = new JTextField();

    // Add pairs to grid
    formPanel.add(new JLabel("Building Name:")); formPanel.add(nameField);
    formPanel.add(new JLabel("Address:"));       formPanel.add(addressField);
    formPanel.add(new JLabel("Price (CZK):"));     formPanel.add(priceField);
    formPanel.add(new JLabel("Bedrooms:"));      formPanel.add(bedsField);
    formPanel.add(new JLabel("Area (m²):"));     formPanel.add(areaField);

    // Description 
    JPanel centerWrapper = new JPanel(new BorderLayout(0, 5));
    centerWrapper.add(formPanel, BorderLayout.NORTH);
    
    descArea = new JTextArea(4, 20);
    descArea.setLineWrap(true);
    descArea.setWrapStyleWord(true);
    JScrollPane descScroll = new JScrollPane(descArea);
    descScroll.setBorder(BorderFactory.createTitledBorder("Description"));
    centerWrapper.add(descScroll, BorderLayout.CENTER);

    //Bottom Section: Actions
    JPanel actionWrapper = new JPanel(new GridLayout(2, 1, 0, 5));
    
    JPanel editRow = new JPanel(new GridLayout(1, 3, 5, 0));
    editBtn = new JButton("Edit");
    redrawBtn = new JButton("Redraw"); // Icon removed for space
    deleteBtn = new JButton("Delete");
    deleteBtn.setForeground(Color.RED);
    
    editRow.add(editBtn);
    editRow.add(redrawBtn);
    editRow.add(deleteBtn);

    JPanel saveRow = new JPanel(new GridLayout(1, 2, 5, 0));
    saveBtn = new JButton("Save");
    cancelBtn = new JButton("Cancel");
    saveBtn.setBackground(new Color(144, 238, 144));
    
    saveRow.add(saveBtn);
    saveRow.add(cancelBtn);

    actionWrapper.add(editRow);
    actionWrapper.add(saveRow);

    //Listeners
    redrawBtn.setVisible(false);
    redrawBtn.addActionListener(e -> initiateRedrawFlow());
    editBtn.addActionListener(e -> setEditMode(true));
    saveBtn.addActionListener(e -> saveProperty());
    deleteBtn.addActionListener(e -> deleteProperty());
    cancelBtn.addActionListener(e -> {
        setEditMode(false);
        if (currentProperty != null) populateDetails(currentProperty);
        else clearFields();
    });

    //Final 
    panel.add(topSection, BorderLayout.NORTH);
    panel.add(centerWrapper, BorderLayout.CENTER);
    panel.add(actionWrapper, BorderLayout.SOUTH);

    setEditMode(false);
    return panel;
}
    /**
     * Initiates the workflow for creating a new property.
     * Steps:
     * 1. Clears existing form data.
     * 2. Prompts the user to draw the property on the map.
     * 3. Sets the application state to 'creating'.
     */
    private void initiateCreateFlow() {
        isCreating = true;
        currentProperty = null;
        tempGeometry = null;
        clearFields();
        setEditMode(true);

        JOptionPane.showMessageDialog(this,
                "Draw the property area on the map. You can use the panel below to select appropriate geometry");

        if (mapPanel != null) {
            drawSettingsPanel.setVisible(true);
            mapPanel.setCurrentTool(new DrawTool(drawSettingsPanel, geometry -> {
                this.tempGeometry = geometry;
                drawSettingsPanel.setVisible(false);
                mapPanel.clearOverlays();
                mapPanel.addOverlay(geometry, new Color(0, 255, 0, 100), "New Property");
                mapPanel.setCurrentTool(new NavigationTool());
                JOptionPane.showMessageDialog(this, "Area captured. Next fill in the details and click Save.");
            }));
        }
    }

    private void initiateRedrawFlow() {
        if (currentProperty == null)
            return;

        JOptionPane.showMessageDialog(this, "Draw the new property area on the map.");

        if (mapPanel != null) {
            drawSettingsPanel.setVisible(true);
            mapPanel.setCurrentTool(new DrawTool(drawSettingsPanel, geometry -> {
                this.tempGeometry = geometry;
                drawSettingsPanel.setVisible(false);
                mapPanel.clearOverlays();
                mapPanel.addOverlay(geometry, new Color(0, 255, 0, 100), "New Area");
                mapPanel.setCurrentTool(new NavigationTool());
                JOptionPane.showMessageDialog(this, "New area captured! Click Save to apply changes.");
            }));
        }
    }

    /**
     * Saves the property data to the database.
     * This method handles both creating new properties (INSERT) and updating
     * existing ones (UPDATE).
     * It validates input, constructs the {@link Property} object, and calls the
     * appropriate repository method.
     */
    private void saveProperty() {
        try {
            // Validation
            if (nameField.getText().isEmpty() || priceField.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Name and Price are required!");
                return;
            }

            // Object Construction from Form Data
            Property p = new Property();
            p.setBuildingName(nameField.getText());
            p.setAddress(addressField.getText());
            p.setPrice(Double.parseDouble(priceField.getText()));

            if (!bedsField.getText().isEmpty()) {
                p.setNumBedrooms(Integer.parseInt(bedsField.getText()));
            }
            if (!areaField.getText().isEmpty()) {
                p.setSquareMeters(Double.parseDouble(areaField.getText()));
            }
            p.setDescription(descArea.getText());
            Long currentUserId = null;
            if (UserSession.getInstance().getCurrentUser() != null) {
                currentUserId = UserSession.getInstance().getCurrentUser().getUserId();
            }
            p.setListingUserId(currentUserId != null ? currentUserId : 1L);

            // Database Interaction
            try (Connection conn = DBConnection.getConnection()) {
                PropertyQueries queries = new PropertyQueries(conn);

                if (isCreating) {
                    // --- CREATE LOGIC ---
                    if (tempGeometry == null) {
                        JOptionPane.showMessageDialog(this, "Please draw the property area first!");
                        return;
                    }
                    p.setJGeometry(tempGeometry);
                    p.setUnitNumber("1A"); // Default unit
                    queries.createProperty(p);
                    JOptionPane.showMessageDialog(this, "Property Created!");
                } else {
                    // --- UPDATE LOGIC ---
                    p.setPropertyId(currentProperty.getPropertyId());

                    // Use new geometry if drawn, otherwise keep old
                    if (tempGeometry != null) {
                        p.setJGeometry(tempGeometry);
                    } else {
                        p.setJGeometry(currentProperty.getJGeometry());
                    }

                    p.setUnitNumber(currentProperty.getUnitNumber());
                    queries.updateProperty(p);
                    JOptionPane.showMessageDialog(this, "Property Updated!");
                }

                // 4. Cleanup and Refresh
                setEditMode(false);
                isCreating = false;
                mapPanel.clearOverlays();
                loadEverything(); // Reload map and list to show changes

            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving: " + ex.getMessage());
        }
    }

    /**
     * Deletes the currently selected property after confirmation.
     */
    private void deleteProperty() {
        if (currentProperty == null) {
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this property?");
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DBConnection.getConnection()) {
                PropertyQueries queries = new PropertyQueries(conn);
                queries.deleteProperty(currentProperty.getPropertyId());

                // Reset UI
                clearFields();
                currentProperty = null;
                mapPanel.clearOverlays();
                loadEverything(); // Reload data
                JOptionPane.showMessageDialog(this, "Property Deleted.");
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error deleting: " + ex.getMessage());
            }
        }
    }

    /**
     * Reloads all data layers (Properties, Schools, Transit, Cellular) from the
     * database.
     * This is usually called on startup or after data modification.
     */
    private void loadEverything() {
        try {
            Connection conn = DBConnection.getConnection();

            // Instantiate Data Access Objects
            PropertyQueries propertyQueries = new PropertyQueries(conn);
            SchoolQueries schoolQueries = new SchoolQueries(conn);
            TransitQueries transitQueries = new TransitQueries(conn);
            CellularQueries cellularQueries = new CellularQueries(conn);

            // Fetch Data
            List<Property> properties = propertyQueries.getAllProperties();
            List<School> schools = schoolQueries.getAll();
            List<Transit> stops = transitQueries.getAll();
            List<Cellular> coverages = cellularQueries.getAll();

            // Update UI components
            updateView(properties); // Updates Table and Map Property Layer
            mapPanel.setSchools(schools);
            mapPanel.setTransitStops(stops);
            mapPanel.setCellularCoverage(coverages);

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading map data: " + e.getMessage());
        }
    }

    /**
     * Filters properties based on the search text and price slider.
     */
    private void filterProperties() {
        try {
            Connection conn = DBConnection.getConnection();
            PropertyQueries queries = new PropertyQueries(conn);

            String searchText = searchField.getText();
            if (searchText.equals("Search")) {
                searchText = ""; // Treat default placeholder as empty
            }

            double maxPrice = priceSlider.getValue();

            // Fetch filtered results
            List<Property> properties = queries.filterProperties(searchText, maxPrice);
            updateView(properties);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error filtering properties: " + e.getMessage());
        }
    }

    private void filterPropertiesByGeometry(JGeometry geometry) {
        drawSettingsPanel.setVisible(false);

        // 1. Run Query
        // this part is a mess but it works
        try (Connection conn = DBConnection.getConnection()) {
            PropertyQueries queries = new PropertyQueries(conn);
            List<Property> props = queries
                    .getPropertiesIntersecting(geometry);
            updateView(props);
            JOptionPane.showMessageDialog(this, "Found " + props.size() + " properties in area.");
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error searching area: " + ex.getMessage());
        }

        // 2. Show Overlay
        mapPanel.clearOverlays();
        mapPanel.addOverlay(geometry, new Color(255, 0, 0, 50), "Search Area");

        // 3. Reset Tool
        mapPanel.setCurrentTool(new NavigationTool());
    }

    // ==============================================================================================
    // HELPER METHODS
    // ==============================================================================================

    /**
     * Updates the Results Table and the Map visualization with a list of
     * properties.
     *
     * @param properties The list of properties to display.
     */
    private void updateView(List<Property> properties) {
        this.allProperties = properties;

        // Update Table
        // Note: We clear the table and rebuild it.
        tableModel.setRowCount(0);
        for (Property p : properties) {
            // Warning: Array size (5) exceeds defined columns (4) in initialization.
            // DefaultTableModel typically ignores extra columns in view but stores data.
            tableModel.addRow(new Object[] {
                    p.getPropertyId(), // Index 0 (likely hidden or first col)
                    p.getBuildingName(),
                    p.getAddress(),
                    p.getPrice(),
                    p.getNumBedrooms()
            });
        }

        // Update Map Layers
        mapPanel.setProperties(properties);
    }

    /**
     * Populates the Right Panel fields with data from a specific property.
     *
     * @param p The property to display.
     */
    private void populateDetails(Property p) {
        this.currentProperty = p;
        this.tempGeometry = null; // Reset temp geometry
        isCreating = false;

        // Fill Text Fields
        nameField.setText(p.getBuildingName());
        addressField.setText(p.getAddress());
        priceField.setText(String.valueOf(p.getPrice()));
        bedsField.setText(p.getNumBedrooms() != null ? String.valueOf(p.getNumBedrooms()) : "");
        areaField.setText(p.getSquareMeters() != null ? String.valueOf(p.getSquareMeters()) : "");
        descArea.setText(p.getDescription());

        // Highlight selected property on map
        mapPanel.clearOverlays();
        if (p.getJGeometry() != null) {
            mapPanel.addOverlay(p.getJGeometry(), new Color(255, 255, 0, 100), "Selected");
        }

        // Load Images
        loadImages(p.getPropertyId());
    }

    /**
     * Loads images for a specific property.
     *
     * @param propertyId The ID of the property to load images for.
     */
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

        // Load binary data in background or just direct here (it's small enough
        // usually)
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
                imageLabel.setText("Image Data Missing");
            }
        } catch (Exception e) {
            e.printStackTrace();
            imageLabel.setText("Error");
        }
    }

    private void navigateImage(int direction) {
        if (currentImages == null || currentImages.isEmpty())
            return;
        currentImageIndex += direction;
        displayCurrentImage();
    }

    private void addImage() {
        if (currentProperty == null) {
            JOptionPane.showMessageDialog(this, "Select a property first.");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try (Connection conn = DBConnection.getConnection()) {
                PropertyImageQueries imgQueries = new PropertyImageQueries(conn);
                imgQueries.uploadImage(currentProperty.getPropertyId(), file.getName(), file.getAbsolutePath());
                JOptionPane.showMessageDialog(this, "Image Uploaded!");
                loadImages(currentProperty.getPropertyId());
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Upload Failed: " + e.getMessage());
            }
        }
    }

    private void deleteImage() {
        if (currentImages == null || currentImages.isEmpty())
            return;

        int confirm = JOptionPane.showConfirmDialog(this, "Delete this image?");
        if (confirm == JOptionPane.YES_OPTION) {
            PropertyImage img = currentImages.get(currentImageIndex);
            try (Connection conn = DBConnection.getConnection()) {
                PropertyImageQueries imgQueries = new PropertyImageQueries(conn);
                imgQueries.deleteImage(img.getImageId());
                JOptionPane.showMessageDialog(this, "Image Deleted.");
                loadImages(currentProperty.getPropertyId());
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Delete Failed: " + e.getMessage());
            }
        }
    }

    /**
     * Toggles the editable state of the details form.
     *
     * @param editable True to enable editing/buttons, False for read-only.
     */
    private void setEditMode(boolean editable) {
        // Toggle Fields
        nameField.setEditable(editable);
        addressField.setEditable(editable);
        priceField.setEditable(editable);
        bedsField.setEditable(editable);
        areaField.setEditable(editable);
        descArea.setEditable(editable);

        // Toggle Buttons
        editBtn.setVisible(!editable);
        deleteBtn.setVisible(!editable);
        createBtn.setVisible(!editable);

        redrawBtn.setVisible(editable && !isCreating); // Only show when editing existing

        saveBtn.setVisible(editable);
        cancelBtn.setVisible(editable);
    }

    /**
     * Clears all text fields in the details form.
     */
    private void clearFields() {
        nameField.setText("");
        addressField.setText("");
        priceField.setText("");
        bedsField.setText("");
        areaField.setText("");
        descArea.setText("");
    }

}