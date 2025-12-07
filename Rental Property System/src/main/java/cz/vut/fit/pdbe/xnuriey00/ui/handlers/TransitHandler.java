package cz.vut.fit.pdbe.xnuriey00.ui.handlers;

import cz.vut.fit.pdbe.xnuriey00.model.SpatialEntity;
import cz.vut.fit.pdbe.xnuriey00.model.Transit;
import cz.vut.fit.pdbe.xnuriey00.queries.SpatialEntityQueries;
import cz.vut.fit.pdbe.xnuriey00.queries.TransitQueries;
import cz.vut.fit.pdbe.xnuriey00.ui.map.MapPanel;
import oracle.spatial.geometry.JGeometry;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.util.List;

/**
 * Handler for Transit entity CRUD operations.
 */
public class TransitHandler extends EntityHandler {

    private JTextField nameField;

    @Override
    public String getEntityName() {
        return "Transit";
    }

    @Override
    public String getTableName() {
        return "TRANSIT_STOPS";
    }

    @Override
    public String getIdColumnName() {
        return "STOP_ID";
    }

    @Override
    public SpatialEntityQueries createQueries(Connection conn) {
        return new TransitQueries(conn);
    }

    @Override
    public String[] getTableColumns() {
        return new String[] { "ID", "Name" };
    }

    @Override
    public Object[] getTableRowData(SpatialEntity entity) {
        Transit transit = (Transit) entity;
        return new Object[] { transit.getId(), transit.getName() };
    }

    @Override
    public void populateFormFields(SpatialEntity entity, JPanel formPanel) {
        Transit transit = (Transit) entity;
        if (nameField != null) {
            nameField.setText(transit.getName());
        }
    }

    @Override
    public void clearFormFields(JPanel formPanel) {
        if (nameField != null) {
            nameField.setText("");
        }
    }

    @Override
    public SpatialEntity buildEntityFromForm(JPanel formPanel, JGeometry geometry) {
        Transit transit = new Transit();
        if (nameField != null) {
            transit.setName(nameField.getText());
        }
        transit.setGeometry(geometry);
        return transit;
    }

    @Override
    public boolean validateForm(JPanel formPanel) {
        if (nameField == null || nameField.getText().trim().isEmpty()) {
            return false;
        }
        return true;
    }

    @Override
    public void setMapLayerData(MapPanel mapPanel, List<? extends SpatialEntity> entities) {
        List<Transit> stops = new java.util.ArrayList<Transit>();
        for (SpatialEntity entity : entities) {
            stops.add((Transit) entity);
        }
        mapPanel.setTransitStops(stops);
    }

    /**
     * Creates and returns the form panel with fields for Transit.
     */
    public JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));

        panel.add(new JLabel("Name:"));
        nameField = new JTextField();
        panel.add(nameField);

        return panel;
    }

    public JTextField getNameField() {
        return nameField;
    }

    @Override
    public void updateEntityId(SpatialEntity target, SpatialEntity source) {
        if (target instanceof Transit && source instanceof Transit) {
            ((Transit) target).setStopId(((Transit) source).getStopId());
        }
    }
}
