package cz.vut.fit.pdbe.xnuriey00.ui.handlers;

import cz.vut.fit.pdbe.xnuriey00.model.BikeRoute;
import cz.vut.fit.pdbe.xnuriey00.model.SpatialEntity;
import cz.vut.fit.pdbe.xnuriey00.queries.BikeRouteQueries;
import cz.vut.fit.pdbe.xnuriey00.queries.SpatialEntityQueries;
import cz.vut.fit.pdbe.xnuriey00.ui.map.MapPanel;
import oracle.spatial.geometry.JGeometry;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.util.List;

/**
 * Handler for BikeRoute entity CRUD operations.
 */
public class BikeRouteHandler extends EntityHandler {

    private JTextField nameField;

    @Override
    public String getEntityName() {
        return "Bike Route";
    }

    @Override
    public String getTableName() {
        return "BIKE_ROUTES";
    }

    @Override
    public String getIdColumnName() {
        return "ROUTE_ID";
    }

    @Override
    public SpatialEntityQueries createQueries(Connection conn) {
        return new BikeRouteQueries(conn);
    }

    @Override
    public String[] getTableColumns() {
        return new String[] { "ID", "Name" };
    }

    @Override
    public Object[] getTableRowData(SpatialEntity entity) {
        BikeRoute route = (BikeRoute) entity;
        return new Object[] { route.getId(), route.getName() };
    }

    @Override
    public void populateFormFields(SpatialEntity entity, JPanel formPanel) {
        BikeRoute route = (BikeRoute) entity;
        if (nameField != null) {
            nameField.setText(route.getName());
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
        BikeRoute route = new BikeRoute();
        if (nameField != null) {
            route.setName(nameField.getText());
        }
        route.setGeometry(geometry);
        return route;
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
        List<BikeRoute> routes = new java.util.ArrayList<BikeRoute>();
        for (SpatialEntity entity : entities) {
            routes.add((BikeRoute) entity);
        }
        mapPanel.setBikeRoutes(routes);
    }

    /**
     * Creates and returns the form panel with fields for BikeRoute.
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
        if (target instanceof BikeRoute && source instanceof BikeRoute) {
            ((BikeRoute) target).setRouteId(((BikeRoute) source).getRouteId());
        }
    }
}
