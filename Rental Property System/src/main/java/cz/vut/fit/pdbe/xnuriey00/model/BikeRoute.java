package cz.vut.fit.pdbe.xnuriey00.model;

import oracle.spatial.geometry.JGeometry;

/**
 * Represents a bike route with linestring geometry.
 * 
 * @author eyobed-dev
 */
public class BikeRoute implements SpatialEntity {
      private Long routeId;
      private String name;
      private JGeometry geometry; // LINESTRING

      public BikeRoute() {
      }

      public BikeRoute(Long routeId, String name, JGeometry geometry) {
            this.routeId = routeId;
            this.name = name;
            this.geometry = geometry;
      }

      @Override
      public Long getId() {
            return routeId;
      }

      public Long getRouteId() {
            return routeId;
      }

      public void setRouteId(Long routeId) {
            this.routeId = routeId;
      }

      @Override
      public void setId(Long id) {
            this.routeId = id;
      }

      @Override
      public String getName() {
            return name;
      }

      public void setName(String name) {
            this.name = name;
      }

      @Override
      public JGeometry getGeometry() {
            return geometry;
      }

      @Override
      public void setGeometry(JGeometry geometry) {
            this.geometry = geometry;
      }

      @Override
      public String toString() {
            return "BikeRoute{" +
                        "routeId=" + routeId +
                        ", name='" + name + '\'' +
                        ", geometry=" + geometry +
                        '}';
      }
}
