package cz.vut.fit.pdbe.xnuriey00.model;

import oracle.spatial.geometry.JGeometry;

/**
 * Common interface for all spatial entities.
 * Allows generic handling of geometry editing and identification.
 */
public interface SpatialEntity {
      Long getId();

      String getName(); // Common label/name for the entity

      JGeometry getGeometry();

      void setGeometry(JGeometry geometry);

      void setId(Long id);
}
