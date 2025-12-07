package cz.vut.fit.pdbe.xnuriey00.model;

import oracle.spatial.geometry.JGeometry;

public class Transit implements SpatialEntity {
      private Long stopId;
      private String name;
      private JGeometry geometry;

      public Transit() {
      }

      public Transit(Long stopId, String name, JGeometry geometry) {
            this.stopId = stopId;
            this.name = name;
            this.geometry = geometry;
      }

      @Override
      public Long getId() {
            return stopId;
      }

      public Long getStopId() {
            return stopId;
      }

      public void setStopId(Long stopId) {
            this.stopId = stopId;
      }

      @Override
      public void setId(Long id) {
            this.stopId = id;
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
}
