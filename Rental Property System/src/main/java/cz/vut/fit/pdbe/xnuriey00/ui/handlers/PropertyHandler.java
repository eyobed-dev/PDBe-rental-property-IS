package cz.vut.fit.pdbe.xnuriey00.ui.handlers;

import cz.vut.fit.pdbe.xnuriey00.model.Property;
import cz.vut.fit.pdbe.xnuriey00.model.SpatialEntity;
import cz.vut.fit.pdbe.xnuriey00.queries.PropertyQueries;
import cz.vut.fit.pdbe.xnuriey00.queries.SpatialEntityQueries;
import cz.vut.fit.pdbe.xnuriey00.ui.map.MapPanel;
import cz.vut.fit.pdbe.xnuriey00.util.UserSession;
import oracle.spatial.geometry.JGeometry;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.util.List;

/**
 * Handler for Property entity CRUD operations.
 * Properties are more complex than other entities as they have many fields
 * and support image management.
 */
public class PropertyHandler extends EntityHandler {

    private JTextField buildingNameField;
    private JTextField addressField;
    private JTextField priceField;
    private JTextField bedsField;
    private JTextArea descArea;

    @Override
    public String getEntityName() {
        return "Property";
    }

    @Override
    public String getTableName() {
        return "PROPERTIES";
    }

    @Override
    public String getIdColumnName() {
        return "PROPERTY_ID";
    }

    @Override
    public SpatialEntityQueries createQueries(Connection conn) {
        return new PropertyQueries(conn);
    }

    @Override
    public String[] getTableColumns() {
        return new String[] { "ID", "Name", "Address", "Price", "Bedrooms" };
    }

    @Override
    public Object[] getTableRowData(SpatialEntity entity) {
        Property prop = (Property) entity;
        return new Object[] {
                prop.getPropertyId(),
                prop.getBuildingName(),
                prop.getAddress(),
                prop.getPrice(),
                prop.getNumBedrooms()
        };
    }

    @Override
    public void populateFormFields(SpatialEntity entity, JPanel formPanel) {
        Property prop = (Property) entity;
        if (buildingNameField != null) {
            buildingNameField.setText(prop.getBuildingName());
        }
        if (addressField != null) {
            addressField.setText(prop.getAddress());
        }
        if (priceField != null) {
            priceField.setText(String.valueOf(prop.getPrice()));
        }
        if (bedsField != null) {
            bedsField.setText(prop.getNumBedrooms() != null ? String.valueOf(prop.getNumBedrooms()) : "");
        }
        if (descArea != null) {
            descArea.setText(prop.getDescription());
        }
    }

    @Override
    public void clearFormFields(JPanel formPanel) {
        if (buildingNameField != null) {
            buildingNameField.setText("");
        }
        if (addressField != null) {
            addressField.setText("");
        }
        if (priceField != null) {
            priceField.setText("");
        }
        if (bedsField != null) {
            bedsField.setText("");
        }
        if (descArea != null) {
            descArea.setText("");
        }
    }

    @Override
    public SpatialEntity buildEntityFromForm(JPanel formPanel, JGeometry geometry) {
        Property prop = new Property();

        if (buildingNameField != null) {
            prop.setBuildingName(buildingNameField.getText());
        }
        if (addressField != null) {
            prop.setAddress(addressField.getText());
        }
        if (priceField != null && !priceField.getText().trim().isEmpty()) {
            try {
                prop.setPrice(Double.parseDouble(priceField.getText()));
            } catch (NumberFormatException e) {
                // Will be caught by validation
            }
        }
        if (bedsField != null && !bedsField.getText().trim().isEmpty()) {
            try {
                prop.setNumBedrooms(Integer.parseInt(bedsField.getText()));
            } catch (NumberFormatException e) {
                // Will be caught by validation
            }
        }
        if (descArea != null) {
            prop.setDescription(descArea.getText());
        }

        prop.setGeometry(geometry);

        // Set user ID
        Long currentUserId = null;
        if (UserSession.getInstance().getCurrentUser() != null) {
            currentUserId = UserSession.getInstance().getCurrentUser().getUserId();
        }
        prop.setListingUserId(currentUserId != null ? currentUserId : 1L);

        return prop;
    }

    @Override
    public boolean validateForm(JPanel formPanel) {
        if (buildingNameField == null || buildingNameField.getText().trim().isEmpty()) {
            return false;
        }
        if (priceField == null || priceField.getText().trim().isEmpty()) {
            return false;
        }
        // Validate numeric fields
        if (priceField != null && !priceField.getText().trim().isEmpty()) {
            try {
                Double.parseDouble(priceField.getText());
            } catch (NumberFormatException e) {
                return false;
            }
        }
        if (bedsField != null && !bedsField.getText().trim().isEmpty()) {
            try {
                Integer.parseInt(bedsField.getText());
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void setMapLayerData(MapPanel mapPanel, List<? extends SpatialEntity> entities) {
        List<Property> properties = new java.util.ArrayList<Property>();
        for (SpatialEntity entity : entities) {
            properties.add((Property) entity);
        }
        mapPanel.setProperties(properties);
    }

    @Override
    public boolean supportsImages() {
        return true;
    }

    /**
     * Creates and returns the form panel with fields for Property.
     */
    public JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));

        panel.add(new JLabel("Building Name:"));
        buildingNameField = new JTextField();
        panel.add(buildingNameField);

        panel.add(new JLabel("Address:"));
        addressField = new JTextField();
        panel.add(addressField);

        panel.add(new JLabel("Price (CZK):"));
        priceField = new JTextField();
        panel.add(priceField);

        panel.add(new JLabel("Bedrooms:"));
        bedsField = new JTextField();
        panel.add(bedsField);

        return panel;
    }

    /**
     * Creates and returns the description text area.
     */
    public JTextArea createDescriptionArea() {
        descArea = new JTextArea(4, 20);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        return descArea;
    }

    // Getters for field access
    public JTextField getBuildingNameField() {
        return buildingNameField;
    }

    public JTextField getAddressField() {
        return addressField;
    }

    public JTextField getPriceField() {
        return priceField;
    }

    public JTextField getBedsField() {
        return bedsField;
    }

    public JTextArea getDescArea() {
        return descArea;
    }

    /**
     * Updates an existing property with new ID.
     */
    public void updatePropertyId(Property prop, Long id) {
        prop.setPropertyId(id);
    }

    /**
     * Updates property with unit number (for existing properties).
     */
    public void updatePropertyUnitNumber(Property prop, String unitNumber) {
        prop.setUnitNumber(unitNumber);
    }

    @Override
    public void updateEntityId(SpatialEntity target, SpatialEntity source) {
        if (target instanceof Property && source instanceof Property) {
            Property targetProp = (Property) target;
            Property sourceProp = (Property) source;
            targetProp.setPropertyId(sourceProp.getPropertyId());
            targetProp.setUnitNumber(sourceProp.getUnitNumber());
        }
    }
}
