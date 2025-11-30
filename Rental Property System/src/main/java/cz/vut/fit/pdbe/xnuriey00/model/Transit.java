package cz.vut.fit.pdbe.xnuriey00.model;

import oracle.spatial.geometry.JGeometry;

public class Transit {
      private Long stopId;
      private String name;
      private JGeometry geometry;

      public Transit(Long stopId, String name, JGeometry geometry) {
            this.stopId = stopId;
            this.name = name;
            this.geometry = geometry;
      }

      public Long getStopId() {
            return stopId;
      }

      public void setStopId(Long stopId) {
            this.stopId = stopId;
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
}
