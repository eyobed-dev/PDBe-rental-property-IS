package cz.vut.fit.pdbe.xnuriey00.ui.handlers;

import cz.vut.fit.pdbe.xnuriey00.model.School;
import cz.vut.fit.pdbe.xnuriey00.model.SpatialEntity;
import cz.vut.fit.pdbe.xnuriey00.queries.SchoolQueries;
import cz.vut.fit.pdbe.xnuriey00.queries.SpatialEntityQueries;
import cz.vut.fit.pdbe.xnuriey00.ui.map.MapPanel;
import oracle.spatial.geometry.JGeometry;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.util.List;

/**
 * Handler for School entity CRUD operations.
 */
public class SchoolHandler extends EntityHandler {

    private JTextField nameField;

    @Override
    public String getEntityName() {
        return "School";
    }

    @Override
    public String getTableName() {
        return "SCHOOL_CATCHMENT_AREAS";
    }

    @Override
    public String getIdColumnName() {
        return "AREA_ID";
    }

    @Override
    public SpatialEntityQueries createQueries(Connection conn) {
        return new SchoolQueries(conn);
    }

    @Override
    public String[] getTableColumns() {
        return new String[] { "ID", "Name" };
    }

    @Override
    public Object[] getTableRowData(SpatialEntity entity) {
        School school = (School) entity;
        return new Object[] { school.getId(), school.getName() };
    }

    @Override
    public void populateFormFields(SpatialEntity entity, JPanel formPanel) {
        School school = (School) entity;
        if (nameField != null) {
            nameField.setText(school.getName());
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
        School school = new School();
        if (nameField != null) {
            school.setName(nameField.getText());
        }
        school.setGeometry(geometry);
        return school;
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
        List<School> schools = new java.util.ArrayList<School>();
        for (SpatialEntity entity : entities) {
            schools.add((School) entity);
        }
        mapPanel.setSchools(schools);
    }

    /**
     * Creates and returns the form panel with fields for School.
     * This method is called by MainWindow to set up the form.
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
        if (target instanceof School && source instanceof School) {
            ((School) target).setAreaId(((School) source).getAreaId());
        }
    }
}
