package cz.vut.fit.pdbe.xnuriey00.model;

import oracle.spatial.geometry.JGeometry;

/**
 * Represents a bike route with linestring geometry.
 * 
 * @author eyobed-dev
 */
public class BikeRoute {
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

      // Getters and Setters
      public Long getRouteId() {
            return routeId;
      }

      public void setRouteId(Long routeId) {
            this.routeId = routeId;
      }

      public String getName() {
            return name;
      }

      public void setName(String name) {
            this.name = name;
      }

      public JGeometry getGeometry() {
            return geometry;
      }

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
