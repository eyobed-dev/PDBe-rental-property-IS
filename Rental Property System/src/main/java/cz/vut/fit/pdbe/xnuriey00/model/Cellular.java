package cz.vut.fit.pdbe.xnuriey00.model;

import oracle.spatial.geometry.JGeometry;

public class Cellular implements SpatialEntity {
      private Long coverageId;
      private String providerName;
      private JGeometry geometry;

      public Cellular() {
      }

      public Cellular(Long coverageId, String providerName, JGeometry geometry) {
            this.coverageId = coverageId;
            this.providerName = providerName;
            this.geometry = geometry;
      }

      @Override
      public Long getId() {
            return coverageId;
      }

      public Long getCoverageId() {
            return coverageId;
      }

      public void setCoverageId(Long coverageId) {
            this.coverageId = coverageId;
      }

      @Override
      public void setId(Long id) {
            this.coverageId = id;
      }

      @Override
      public String getName() {
            return providerName;
      }

      public String getProviderName() {
            return providerName;
      }

      public void setProviderName(String providerName) {
            this.providerName = providerName;
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
