package cz.vut.fit.pdbe.xnuriey00.ui;

import cz.vut.fit.pdbe.xnuriey00.Main;
import cz.vut.fit.pdbe.xnuriey00.model.Property;
import cz.vut.fit.pdbe.xnuriey00.model.SpatialEntity;
import cz.vut.fit.pdbe.xnuriey00.queries.SpatialEntityQueries;
import cz.vut.fit.pdbe.xnuriey00.ui.components.PropertyImagePanel;
import cz.vut.fit.pdbe.xnuriey00.ui.components.SearchPanel;
import cz.vut.fit.pdbe.xnuriey00.ui.handlers.*;
import cz.vut.fit.pdbe.xnuriey00.ui.map.MapControlPanel;
import cz.vut.fit.pdbe.xnuriey00.ui.map.MapPanel;
import cz.vut.fit.pdbe.xnuriey00.ui.map.tools.DrawSettingsPanel;
import cz.vut.fit.pdbe.xnuriey00.ui.map.tools.DrawTool;
import cz.vut.fit.pdbe.xnuriey00.ui.map.tools.EditTool;
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
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The main application window.
 * Handles the primary UI layout, map integration, property management, and
 * image operations. too much but finished
 *
 * @author eyobed-dev
 */
public class MainWindow extends JFrame {

    // Static log area for simple access
    private static JTextArea logArea;
    private SearchPanel searchPanel;
    // tools. Inside ./map
    private MapPanel mapPanel; // The custom panel responsible for rendering the map and handling spatial
    // Right Panel Components
    private JTextArea descArea;
    private JButton editBtn, deleteBtn, saveBtn, cancelBtn, createBtn, redrawBtn;
    // Image Carousel Components
    private PropertyImagePanel propertyImagePanel;
    // Draw Settings
    private DrawSettingsPanel drawSettingsPanel;
    private EntityHandler currentHandler;
    private PropertyHandler propertyHandler;
    private SchoolHandler schoolHandler;
    private TransitHandler transitHandler;
    private CellularHandler cellularHandler;
    private BikeRouteHandler bikeRouteHandler;
    private JToggleButton propertyToggleBtn, schoolToggleBtn, transitToggleBtn, cellularToggleBtn, bikeRouteToggleBtn;
    private List<EntityHandler> handlers;
    // The entity currently selected or being edited.
    private SpatialEntity currentEntity;
    // Temporary storage for geometry drawn on the map during entity creation.
    private JGeometry tempGeometry;
    // Flag indicating if the user is currently in the "Create New" flow.
    private boolean isCreating = false;
    // Form panel container (will be populated dynamically by handlers)
    private JPanel formPanelContainer;

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
                } catch (SQLException ignored) {
                }
            }
        });

        // ============================== Header Content ==============================
        add(createHeader(), BorderLayout.NORTH);

        // ============================== Main Content ===============================
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));

        // IMPORTANT: Initialize map panel FIRST before search panel
        // because SearchPanel needs references to mapPanel and drawSettingsPanel
        contentPanel.add(createCenterMapPanel(), BorderLayout.CENTER);
        contentPanel.add(createLeftControlPanel(), BorderLayout.WEST);
        contentPanel.add(createRightDetailPanel(), BorderLayout.EAST);

        add(contentPanel, BorderLayout.CENTER);

        // Center the window on screen
        setLocationRelativeTo(null);

        // Initialize handlers
        initializeHandlers();

        // Set initial entity type
        switchEntityType(EntityType.PROPERTY);

        // Initial Data Load: Fetches properties, schools, transit, etc.
        loadEverything();
    }

    /**
     * Static method to log queries from anywhere in the app.
     *
     * @param query The SQL query string to log.
     */
    public static void logQuery(String query) {
        if (logArea != null) {
            SwingUtilities.invokeLater(() -> {
                logArea.append(query + "\n--------------------------------------------------\n");
                logArea.setCaretPosition(logArea.getDocument().getLength());
            });
        }
    }

    /**
     * Initializes all entity handlers.
     */
    private void initializeHandlers() {
        propertyHandler = new PropertyHandler();
        schoolHandler = new SchoolHandler();
        transitHandler = new TransitHandler();
        cellularHandler = new CellularHandler();
        bikeRouteHandler = new BikeRouteHandler();

        handlers = new ArrayList<>();
        handlers.add(propertyHandler);
        handlers.add(schoolHandler);
        handlers.add(transitHandler);
        handlers.add(cellularHandler);
        handlers.add(bikeRouteHandler);
    }

    /**
     * Gets the handler for the current entity type.
     */
    private EntityHandler getCurrentHandler() {
        return currentHandler;
    }

    /**
     * Switches the active entity type and updates the UI accordingly.
     */
    private void switchEntityType(EntityType type) {
        // Entity type management

        // Set the appropriate handler
        if (type == EntityType.PROPERTY) {
            currentHandler = propertyHandler;
        } else if (type == EntityType.SCHOOL) {
            currentHandler = schoolHandler;
        } else if (type == EntityType.TRANSIT) {
            currentHandler = transitHandler;
        } else if (type == EntityType.CELLULAR) {
            currentHandler = cellularHandler;
        } else if (type == EntityType.BIKE_ROUTE) {
            currentHandler = bikeRouteHandler;
        }

        // Update toggle button selection
        if (propertyToggleBtn != null) {
            propertyToggleBtn.setSelected(type == EntityType.PROPERTY);
            schoolToggleBtn.setSelected(type == EntityType.SCHOOL);
            transitToggleBtn.setSelected(type == EntityType.TRANSIT);
            cellularToggleBtn.setSelected(type == EntityType.CELLULAR);
            bikeRouteToggleBtn.setSelected(type == EntityType.BIKE_ROUTE);
        }

        // Reload data and refresh UI
        loadCurrentEntityData();
        refreshFormPanel();
        clearFields();
        currentEntity = null;
        updateCreateButtonText();
        updateImagePanelVisibility();
    }

    /**
     * Shows/hides image panel based on whether current entity supports images.
     */
    private void updateImagePanelVisibility() {
        if (propertyImagePanel == null) {
            return;
        }
        if (currentHandler != null) {
            propertyImagePanel.setCurrentEntity(currentEntity);
            propertyImagePanel.setVisible(currentHandler.supportsImages());
        }
    }

    /**
     * Updates the create button text based on current entity type.
     */
    private void updateCreateButtonText() {
        if (createBtn != null && currentHandler != null) {
            createBtn.setText(currentHandler.getCreateButtonText());
        }
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
        title.setIcon(new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource("logo.png"))));

        // Entity Type Selection (Toggle Buttons)
        JPanel entityTypePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        entityTypePanel.setBorder(BorderFactory.createTitledBorder("Entity Type"));
        ButtonGroup entityTypeButtonGroup = new ButtonGroup();

        propertyToggleBtn = new JToggleButton("Properties", true);
        schoolToggleBtn = new JToggleButton("Schools");
        transitToggleBtn = new JToggleButton("Transit");
        cellularToggleBtn = new JToggleButton("Cellular");
        bikeRouteToggleBtn = new JToggleButton("Bike Routes");

        entityTypeButtonGroup.add(propertyToggleBtn);
        entityTypeButtonGroup.add(schoolToggleBtn);
        entityTypeButtonGroup.add(transitToggleBtn);
        entityTypeButtonGroup.add(cellularToggleBtn);
        entityTypeButtonGroup.add(bikeRouteToggleBtn);

        propertyToggleBtn.addActionListener(e -> switchEntityType(EntityType.PROPERTY));
        schoolToggleBtn.addActionListener(e -> switchEntityType(EntityType.SCHOOL));
        transitToggleBtn.addActionListener(e -> switchEntityType(EntityType.TRANSIT));
        cellularToggleBtn.addActionListener(e -> switchEntityType(EntityType.CELLULAR));
        bikeRouteToggleBtn.addActionListener(e -> switchEntityType(EntityType.BIKE_ROUTE));

        entityTypePanel.add(propertyToggleBtn);
        entityTypePanel.add(schoolToggleBtn);
        entityTypePanel.add(transitToggleBtn);
        entityTypePanel.add(cellularToggleBtn);
        entityTypePanel.add(bikeRouteToggleBtn);

        // Button Container
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton refreshBtn = new JButton("🔄 REFRESH");
        JButton helpBtn = new JButton("❓ HELP");
        JButton logoutBtn = new JButton("⏻ LOGOUT");

        // Action Listeners
        refreshBtn.addActionListener(e -> loadEverything());
        helpBtn.addActionListener(e -> new HelpWindow().setVisible(true));

        // Logout Logic: Clear session, close window, and relaunch Main login
        logoutBtn.addActionListener(e -> {
            UserSession.getInstance().logout();
            this.dispose();
            Main.launch();
        });

        // Admin only feature check
        if (UserSession.getInstance().isAdmin()) {
            JButton manageUsers = new JButton("\uD83D\uDC65 MANAGE USERS");
            manageUsers.addActionListener(e -> new UserManagementWindow().setVisible(true));
            buttonPanel.add(manageUsers);
        }

        buttonPanel.add(refreshBtn);
        buttonPanel.add(helpBtn);
        buttonPanel.add(logoutBtn);

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.add(title, BorderLayout.WEST);
        topRow.add(buttonPanel, BorderLayout.EAST);

        header.add(topRow, BorderLayout.NORTH);
        header.add(entityTypePanel, BorderLayout.CENTER);
        return header;
    }

    /**
     * Creates the left side control panel containing search tools and results list.
     *
     * @return A constructed JPanel for the left sidebar.
     */
    private JPanel createLeftControlPanel() {
        searchPanel = new SearchPanel(mapPanel, drawSettingsPanel, this::selectEntity, this::updateView,
                this::loadEverything);
        return searchPanel;
    }

    private SpatialEntity getCurrentEntity() {
        return currentEntity;
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

        // Wire up selection listener
        mapPanel.addSelectionListener(this::selectEntity);

        // Add standard map controls and settings at the bottom
        JPanel bottomPanel = new JPanel(new BorderLayout());

        MapControlPanel controlPanel = new MapControlPanel(mapPanel);
        bottomPanel.add(controlPanel, BorderLayout.EAST);

        // Draw Settings (Initially Hidden)
        drawSettingsPanel = new DrawSettingsPanel();
        drawSettingsPanel.setVisible(false);
        bottomPanel.add(drawSettingsPanel, BorderLayout.WEST);

        mapContainer.add(bottomPanel, BorderLayout.SOUTH);

        // SQL Log Area
        logArea = new JTextArea(5, 50);
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(BorderFactory.createTitledBorder("SQL Query Log"));

        JPanel southContainer = new JPanel(new BorderLayout());
        southContainer.add(bottomPanel, BorderLayout.NORTH);
        southContainer.add(logScroll, BorderLayout.CENTER);

        mapContainer.add(southContainer, BorderLayout.SOUTH);

        return mapContainer;
    }

    /**
     * Creates the right-side details panel for viewing and editing properties.
     *
     * @return A constructed JPanel for details.
     */
    private JPanel createRightDetailPanel() {
        // Main Container
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setPreferredSize(new Dimension(320, 0));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Top Section
        JPanel topSection = new JPanel();
        topSection.setLayout(new BoxLayout(topSection, BoxLayout.Y_AXIS));

        createBtn = new JButton("➕ Create New");
        createBtn.setBackground(new Color(144, 238, 144));
        createBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        createBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35)); // Full width
        createBtn.addActionListener(e -> initiateCreateFlow());

        // Update button text when entity type changes
        updateCreateButtonText();

        topSection.add(createBtn);
        topSection.add(Box.createVerticalStrut(10));

        // Image panel
        propertyImagePanel = new PropertyImagePanel();
        topSection.add(propertyImagePanel);

        // Center Section: Data Form (Dynamic - populated by handler)
        formPanelContainer = new JPanel(new BorderLayout(0, 5));

        // Form panel will be populated by refreshFormPanel()
        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        formPanelContainer.add(formPanel, BorderLayout.NORTH);

        // Description area (only for Properties)
        descArea = new JTextArea(4, 20);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setBorder(BorderFactory.createTitledBorder("Description"));
        formPanelContainer.add(descScroll, BorderLayout.CENTER);

        // Initialize form fields for Property (will be updated by refreshFormPanel)
        // Fields are now managed by handlers

        // Bottom Section: Actions
        JPanel actionWrapper = new JPanel(new GridLayout(2, 1, 0, 5));

        JPanel editRow = new JPanel(new GridLayout(1, 3, 5, 0));
        editBtn = new JButton("Edit");
        redrawBtn = new JButton("Redraw");
        deleteBtn = new JButton("Delete");
        deleteBtn.setBackground(new Color(255, 170, 170, 194));

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

        // Listeners
        redrawBtn.setVisible(false);
        redrawBtn.addActionListener(e -> initiateRedrawFlow());
        editBtn.addActionListener(e -> setEditMode(true));
        saveBtn.addActionListener(e -> saveCurrentEntity());
        deleteBtn.addActionListener(e -> deleteCurrentEntity());
        cancelBtn.addActionListener(e -> {
            setEditMode(false);
            if (currentEntity != null)
                populateDetails(currentEntity);
            else
                clearFields();
        });

        // Final
        panel.add(topSection, BorderLayout.NORTH);
        panel.add(formPanelContainer, BorderLayout.CENTER);

        // Combine actions and spatial calculations in the south
        JPanel southContainer = new JPanel(new BorderLayout());
        southContainer.add(actionWrapper, BorderLayout.NORTH);

        cz.vut.fit.pdbe.xnuriey00.ui.components.SpatialCalculationPanel calcPanel = new cz.vut.fit.pdbe.xnuriey00.ui.components.SpatialCalculationPanel(
                () -> currentEntity);
        southContainer.add(calcPanel, BorderLayout.SOUTH);

        panel.add(southContainer, BorderLayout.SOUTH);

        setEditMode(false);
        return panel;
    }

    /**
     * Refreshes the form panel based on the current entity type.
     * This method is called when switching entity types.
     */
    private void refreshFormPanel() {
        if (formPanelContainer == null || currentHandler == null) {
            return;
        }

        // Remove all existing components
        formPanelContainer.removeAll();

        // Create new form panel from handler
        JPanel formPanel = currentHandler.createFormPanel();
        if (formPanel != null) {
            formPanelContainer.add(formPanel, BorderLayout.NORTH);
        }

        // Handle description area if supported
        descArea = currentHandler.createDescriptionArea();
        if (descArea != null) {
            JScrollPane descScroll = new JScrollPane(descArea);
            descScroll.setBorder(BorderFactory.createTitledBorder("Description"));
            formPanelContainer.add(descScroll, BorderLayout.CENTER);
        }

        formPanelContainer.revalidate();
        formPanelContainer.repaint();
    }

    /**
     * Initiates the workflow for creating a new entity.
     * Steps:
     * 1. Clears existing form data.
     * 2. Prompts the user to draw the entity on the map.
     * 3. Sets the application state to 'creating'.
     */
    private void initiateCreateFlow() {
        if (currentHandler == null) {
            return;
        }
        isCreating = true;
        currentEntity = null;
        tempGeometry = null;
        clearFields();
        setEditMode(true);

        String entityName = currentHandler.getEntityName();
        JOptionPane.showMessageDialog(this, "Draw the " + entityName.toLowerCase()
                + " area on the map. You can use the panel below to select appropriate geometry");

        if (mapPanel != null) {
            drawSettingsPanel.setVisible(true);
            final EntityHandler handler = currentHandler;
            mapPanel.setCurrentTool(new DrawTool(drawSettingsPanel, geometry -> {
                tempGeometry = geometry;
                drawSettingsPanel.setVisible(false);
                mapPanel.clearOverlays();
                mapPanel.addOverlay(geometry, new Color(0, 255, 0, 100), "New " + handler.getEntityName());
                mapPanel.setCurrentTool(new NavigationTool());
                JOptionPane.showMessageDialog(MainWindow.this,
                        "Area captured. Next fill in the details and click Save.");
            }));
        }
    }

    private void initiateRedrawFlow() {
        if (currentEntity == null || currentHandler == null)
            return;

        String entityName = currentHandler.getEntityName();
        JOptionPane.showMessageDialog(this, "Draw the new " + entityName.toLowerCase() + " area on the map.");

        if (mapPanel != null) {
            drawSettingsPanel.setVisible(true);
            final SpatialEntity entity = currentEntity;
            mapPanel.setCurrentTool(new DrawTool(drawSettingsPanel, geometry -> {
                tempGeometry = geometry;
                drawSettingsPanel.setVisible(false);
                mapPanel.clearOverlays();
                mapPanel.addOverlay(geometry, new Color(248, 255, 167, 85), "New Area");

                // Update current entity geometry immediately for visual feedback
                entity.setGeometry(geometry);
                mapPanel.setCurrentTool(new EditTool(entity)); // Switch back to EditTool

                JOptionPane.showMessageDialog(MainWindow.this,
                        "New area captured! You can now adjust it or click Save.");
            }));
        }
    }

    /**
     * Reloads all data layers (Properties, Schools, Transit, Cellular, Bike Routes)
     * from the
     * database.
     * This is usually called on startup or after data modification.
     */
    private void loadEverything() {
        try (Connection conn = DBConnection.getConnection()) {
            for (EntityHandler handler : handlers) {
                handler.loadData(conn, mapPanel);
            }

            // Update table for current entity type
            loadCurrentEntityData();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading map data: " + e.getMessage());
        }
    }

    /**
     * Loads data for the current entity type and updates the table.
     */
    private void loadCurrentEntityData() {
        if (currentHandler == null) {
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            SpatialEntityQueries queries = currentHandler.createQueries(conn);
            List<? extends SpatialEntity> entities = queries.getAll();
            updateView(entities);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading " + currentHandler.getEntityName() + " data: " + e.getMessage());
        }
    }

    /**
     * Updates the Results Table and the Map visualization with a list of entities.
     *
     * @param entities The list of entities to display.
     */
    private void updateView(List<? extends SpatialEntity> entities) {
        if (currentHandler == null) {
            return;
        }

        // Cache of all currently loaded entities to avoid constant DB fetching for
        // details.

        if (searchPanel != null) {
            searchPanel.updateTable(entities, currentHandler);
        }

        // Update Map Layer
        currentHandler.setMapLayerData(mapPanel, entities);
    }

    /**
     * Clears all input fields in the details panel.
     */
    private void clearFields() {
        if (currentHandler != null && formPanelContainer != null) {
            // Find the form panel (first component)
            if (formPanelContainer.getComponentCount() > 0) {
                Component comp = formPanelContainer.getComponent(0);
                if (comp instanceof JPanel) {
                    currentHandler.clearFormFields((JPanel) comp);
                }
            }
        }
        if (descArea != null) {
            descArea.setText("");
        }
        if (propertyImagePanel != null) {
            propertyImagePanel.clearImages();
        }
    }

    /**
     * Populates the details panel with data from the selected entity.
     *
     * @param entity The entity to display.
     */
    private void populateDetails(SpatialEntity entity) {
        if (entity == null || currentHandler == null)
            return;

        currentEntity = entity;

        // Populate form fields via handler
        if (formPanelContainer.getComponentCount() > 0) {
            Component comp = formPanelContainer.getComponent(0);
            if (comp instanceof JPanel) {
                currentHandler.populateFormFields(entity, (JPanel) comp);
            }
        }

        // Populate description if supported
        if (descArea != null && entity instanceof Property) {
            descArea.setText(((Property) entity).getDescription());
        }

        // Load images if supported
        if (currentHandler.supportsImages() && propertyImagePanel != null) {
            propertyImagePanel.setCurrentEntity(entity);
        } else if (propertyImagePanel != null) {
            propertyImagePanel.clearImages();
        }
        setEditMode(false);
    }

    /**
     * Saves the current entity (create or update).
     */
    private void saveCurrentEntity() {
        if (currentHandler == null)
            return;

        // Get form panel
        JPanel formPanel = null;
        if (formPanelContainer.getComponentCount() > 0) {
            Component comp = formPanelContainer.getComponent(0);
            if (comp instanceof JPanel) {
                formPanel = (JPanel) comp;
            }
        }

        if (formPanel == null)
            return;

        // Validate form
        if (!currentHandler.validateForm(formPanel)) {
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            // Build entity from form
            JGeometry geometry = (currentEntity != null) ? currentEntity.getGeometry() : tempGeometry;

            // If creating new entity, ensure geometry is set
            if (isCreating && geometry == null) {
                JOptionPane.showMessageDialog(this, "Please draw the area on the map first.");
                return;
            }

            SpatialEntity entity = currentHandler.buildEntityFromForm(formPanel, geometry);

            // Set ID if updating
            if (!isCreating && currentEntity != null) {
                entity.setId(currentEntity.getId());
            }

            // Handle description for Property
            if (entity instanceof Property && descArea != null) {
                ((Property) entity).setDescription(descArea.getText());
            }

            // Save to DB
            if (isCreating) {
                currentHandler.createQueries(conn).create(entity);
                JOptionPane.showMessageDialog(this, "Created successfully!");
            } else {
                currentHandler.createQueries(conn).update(entity);
                JOptionPane.showMessageDialog(this, "Updated successfully!");
            }

            // Refresh UI
            loadEverything();
            setEditMode(false);
            isCreating = false;
            tempGeometry = null;

            // Select the saved entity
            if (currentEntity != null) {
                currentEntity = null;
                clearFields();
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error saving: " + e.getMessage());
        }
    }

    /**
     * Deletes the currently selected entity.
     */
    private void deleteCurrentEntity() {
        if (currentEntity == null || currentHandler == null)
            return;

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this " + currentHandler.getEntityName().toLowerCase() + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DBConnection.getConnection()) {
                currentHandler.createQueries(conn).delete(currentEntity.getId());
                JOptionPane.showMessageDialog(this, "Deleted successfully!");
                loadEverything();
                clearFields();
                currentEntity = null;
                mapPanel.clearOverlays();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error deleting: " + e.getMessage());
            }
        }
    }

    /**
     * Retrieves the ID of the currently selected entity.
     *
     * @return The ID of the selected entity, or null if no entity is selected.
     */
    private Long getSelectedEntityId() {
        if (currentEntity != null) {
            return currentEntity.getId();
        }
        return null;
    }

    /**
     * Toggles the editable state of the details form.
     *
     * @param editable True to enable editing/buttons, False for read-only.
     */
    private void setEditMode(boolean editable) {
        if (formPanelContainer == null) {
            return;
        }

        // Toggle Fields - iterate through all components in form panel
        setFieldsEditable(formPanelContainer, editable);
        if (descArea != null) {
            descArea.setEditable(editable);
        }

        // Toggle Buttons
        editBtn.setVisible(!editable);
        deleteBtn.setVisible(!editable);
        createBtn.setVisible(!editable);

        redrawBtn.setVisible(editable && !isCreating); // Only show when editing existing

        saveBtn.setVisible(editable);
        cancelBtn.setVisible(editable);

        // Switch Tools
        if (mapPanel != null) {
            if (editable && !isCreating && currentEntity != null) {
                mapPanel.setCurrentTool(new EditTool(currentEntity));
            } else {
                mapPanel.setCurrentTool(new NavigationTool());
            }
        }
    }

    /**
     * Recursively sets editable state of all text fields in a container.
     */
    private void setFieldsEditable(Container container, boolean editable) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JTextField) {
                ((JTextField) comp).setEditable(editable);
            } else if (comp instanceof JTextArea) {
                ((JTextArea) comp).setEditable(editable);
            } else if (comp instanceof Container) {
                setFieldsEditable((Container) comp, editable);
            }
        }
    }

    /**
     * Selects an entity, updates the details panel, and syncs the table selection.
     *
     * @param entity The entity to select.
     */
    private void selectEntity(SpatialEntity entity) {
        populateDetails(entity);

        // Sync Table Selection if not already selected
        if (searchPanel != null) {
            JTable table = searchPanel.getTable();
            DefaultTableModel tableModel = searchPanel.getTableModel();

            for (int i = 0; i < tableModel.getRowCount(); i++) {
                Long id = (Long) tableModel.getValueAt(i, 0);
                if (id.equals(entity.getId())) {
                    if (table.getSelectedRow() != i) {
                        table.setRowSelectionInterval(i, i);
                        table.scrollRectToVisible(table.getCellRect(i, 0, true));
                    }
                    break;
                }
            }
        }
    }
}