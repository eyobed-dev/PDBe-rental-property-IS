package cz.vut.fit.pdbe.xnuriey00.ui.handlers;

import cz.vut.fit.pdbe.xnuriey00.model.SpatialEntity;
import cz.vut.fit.pdbe.xnuriey00.queries.SpatialEntityQueries;
import cz.vut.fit.pdbe.xnuriey00.ui.map.MapPanel;
import oracle.spatial.geometry.JGeometry;

import javax.swing.*;
import java.sql.Connection;
import java.util.List;

/**
 * Abstract base class for entity-specific UI handlers.
 * Each entity type (Property, School, Transit, etc.) has a concrete handler
 * that knows how to manage its form fields, table display, and CRUD operations.
 */
public abstract class EntityHandler {

    /**
     * Returns the display name of this entity type.
     */
    public abstract String getEntityName();

    /**
     * Returns the database table name for this entity.
     */
    public abstract String getTableName();

    /**
     * Returns the primary key column name for this entity.
     */
    public abstract String getIdColumnName();

    /**
     * Creates a query object for this entity type.
     */
    public abstract SpatialEntityQueries createQueries(Connection conn);

    /**
     * Returns the column names for the results table.
     */
    public abstract String[] getTableColumns();

    /**
     * Converts an entity to table row data.
     */
    public abstract Object[] getTableRowData(SpatialEntity entity);

    /**
     * Populates form fields with entity data.
     * 
     * @param entity    The entity to display
     * @param formPanel The panel containing form fields
     */
    public abstract void populateFormFields(SpatialEntity entity, JPanel formPanel);

    /**
     * Clears all form fields.
     * 
     * @param formPanel The panel containing form fields
     */
    public abstract void clearFormFields(JPanel formPanel);

    /**
     * Builds an entity object from form field values.
     * 
     * @param formPanel The panel containing form fields
     * @param geometry  The geometry for the entity (may be null)
     * @return A new entity object with form data
     */
    public abstract SpatialEntity buildEntityFromForm(JPanel formPanel, JGeometry geometry);

    /**
     * Validates form input before saving.
     * 
     * @param formPanel The panel containing form fields
     * @return true if valid, false otherwise
     */
    public abstract boolean validateForm(JPanel formPanel);

    /**
     * Updates the map layer with entity data.
     * 
     * @param mapPanel The map panel to update
     * @param entities The list of entities to display
     */
    public abstract void setMapLayerData(MapPanel mapPanel, List<? extends SpatialEntity> entities);

    /**
     * Returns whether this entity type supports images.
     * Only Properties support images.
     */
    public boolean supportsImages() {
        return false;
    }

    /**
     * Creates and returns the form panel for this entity type.
     * The panel should contain all necessary input fields.
     */
    public abstract JPanel createFormPanel();

    /**
     * Creates and returns a description area for this entity type.
     * Returns null if the entity type does not support a description.
     */
    public JTextArea createDescriptionArea() {
        return null;
    }

    /**
     * Returns the label text for the create button.
     */
    public String getCreateButtonText() {
        return "➕ Create New " + getEntityName();
    }

    /**
     * Updates the ID of the target entity with the ID from the source entity.
     * Used when updating an existing entity to preserve its ID.
     * 
     * @param target The entity being updated (has new data)
     * @param source The existing entity (has the ID)
     */
    public abstract void updateEntityId(SpatialEntity target, SpatialEntity source);

    /**
     * Loads all entities of this type from the database and updates the map layer.
     *
     * @param conn     Database connection
     * @param mapPanel Map panel to update
     */
    public void loadData(Connection conn, MapPanel mapPanel) {
        try {
            SpatialEntityQueries queries = createQueries(conn);
            List<? extends SpatialEntity> entities = queries.getAll();
            setMapLayerData(mapPanel, entities);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
