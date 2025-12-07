package cz.vut.fit.pdbe.xnuriey00.queries;

import cz.vut.fit.pdbe.xnuriey00.model.SpatialEntity;
import java.sql.SQLException;
import java.util.List;

/**
 * Common interface for all spatial entity query operations.
 * Provides unified CRUD operations for all spatial entities.
 */
public interface SpatialEntityQueries {
    /**
     * Retrieves all entities of this type from the database.
     * 
     * @return List of all entities
     * @throws SQLException If a database error occurs
     */
    List<? extends SpatialEntity> getAll() throws SQLException;

    /**
     * Creates a new entity in the database.
     * 
     * @param entity The entity to create
     * @throws SQLException If a database error occurs
     */
    void create(SpatialEntity entity) throws SQLException;

    /**
     * Updates an existing entity in the database.
     * 
     * @param entity The entity with updated values
     * @throws SQLException If a database error occurs
     */
    void update(SpatialEntity entity) throws SQLException;

    /**
     * Deletes an entity by its ID.
     * 
     * @param id The ID of the entity to delete
     * @throws SQLException If a database error occurs
     */
    void delete(Long id) throws SQLException;

    /**
     * Retrieves a single entity by its ID.
     * 
     * @param id The entity ID
     * @return The entity, or null if not found
     * @throws SQLException If a database error occurs
     */
    SpatialEntity getById(Long id) throws SQLException;
}

