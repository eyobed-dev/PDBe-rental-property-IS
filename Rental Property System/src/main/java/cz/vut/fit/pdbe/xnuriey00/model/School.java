package cz.vut.fit.pdbe.xnuriey00.model;

import oracle.spatial.geometry.JGeometry;

public class School implements SpatialEntity {
      private Long areaId;
      private String name;
      private JGeometry geometry;

      public School() {
      }

      public School(Long areaId, String name, JGeometry geometry) {
            this.areaId = areaId;
            this.name = name;
            this.geometry = geometry;
      }

      @Override
      public Long getId() {
            return areaId;
      }

      public Long getAreaId() {
            return areaId;
      }

      public void setAreaId(Long areaId) {
            this.areaId = areaId;
      }

      @Override
      public void setId(Long id) {
            this.areaId = id;
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
