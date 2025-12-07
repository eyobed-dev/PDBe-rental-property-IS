package cz.vut.fit.pdbe.xnuriey00.ui.handlers;

import cz.vut.fit.pdbe.xnuriey00.model.Cellular;
import cz.vut.fit.pdbe.xnuriey00.model.SpatialEntity;
import cz.vut.fit.pdbe.xnuriey00.queries.CellularQueries;
import cz.vut.fit.pdbe.xnuriey00.queries.SpatialEntityQueries;
import cz.vut.fit.pdbe.xnuriey00.ui.map.MapPanel;
import oracle.spatial.geometry.JGeometry;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.util.List;

/**
 * Handler for Cellular entity CRUD operations.
 */
public class CellularHandler extends EntityHandler {

    private JTextField providerNameField;

    @Override
    public String getEntityName() {
        return "Cellular";
    }

    @Override
    public String getTableName() {
        return "CELLULAR_COVERAGE";
    }

    @Override
    public String getIdColumnName() {
        return "COVERAGE_ID";
    }

    @Override
    public SpatialEntityQueries createQueries(Connection conn) {
        return new CellularQueries(conn);
    }

    @Override
    public String[] getTableColumns() {
        return new String[] { "ID", "Provider Name" };
    }

    @Override
    public Object[] getTableRowData(SpatialEntity entity) {
        Cellular cellular = (Cellular) entity;
        return new Object[] { cellular.getId(), cellular.getProviderName() };
    }

    @Override
    public void populateFormFields(SpatialEntity entity, JPanel formPanel) {
        Cellular cellular = (Cellular) entity;
        if (providerNameField != null) {
            providerNameField.setText(cellular.getProviderName());
        }
    }

    @Override
    public void clearFormFields(JPanel formPanel) {
        if (providerNameField != null) {
            providerNameField.setText("");
        }
    }

    @Override
    public SpatialEntity buildEntityFromForm(JPanel formPanel, JGeometry geometry) {
        Cellular cellular = new Cellular();
        if (providerNameField != null) {
            cellular.setProviderName(providerNameField.getText());
        }
        cellular.setGeometry(geometry);
        return cellular;
    }

    @Override
    public boolean validateForm(JPanel formPanel) {
        return providerNameField != null && !providerNameField.getText().trim().isEmpty();
    }

    @Override
    public void setMapLayerData(MapPanel mapPanel, List<? extends SpatialEntity> entities) {
        List<Cellular> coverages = new java.util.ArrayList<Cellular>();
        for (SpatialEntity entity : entities) {
            coverages.add((Cellular) entity);
        }
        mapPanel.setCellularCoverage(coverages);
    }

    /**
     * Creates and returns the form panel with fields for Cellular.
     */
    public JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));

        panel.add(new JLabel("Provider Name:"));
        providerNameField = new JTextField();
        panel.add(providerNameField);

        return panel;
    }

    public JTextField getProviderNameField() {
        return providerNameField;
    }

    @Override
    public void updateEntityId(SpatialEntity target, SpatialEntity source) {
        if (target instanceof Cellular && source instanceof Cellular) {
            ((Cellular) target).setCoverageId(((Cellular) source).getCoverageId());
        }
    }
}
