package cz.vut.fit.pdbe.xnuriey00.model;

import oracle.spatial.geometry.JGeometry;

public class SchoolCatchmentArea {
      private Long areaId;
      private String name;
      private JGeometry geometry;

      public SchoolCatchmentArea(Long areaId, String name, JGeometry geometry) {
            this.areaId = areaId;
            this.name = name;
            this.geometry = geometry;
      }

      public Long getAreaId() {
            return areaId;
      }

      public void setAreaId(Long areaId) {
            this.areaId = areaId;
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
