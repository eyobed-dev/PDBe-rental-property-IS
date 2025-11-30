package cz.vut.fit.pdbe.xnuriey00.model;

import oracle.spatial.geometry.JGeometry;

public class Cellular {
      private Long coverageId;
      private String providerName;
      private JGeometry geometry;

      public Cellular(Long coverageId, String providerName, JGeometry geometry) {
            this.coverageId = coverageId;
            this.providerName = providerName;
            this.geometry = geometry;
      }

      public Long getCoverageId() {
            return coverageId;
      }

      public void setCoverageId(Long coverageId) {
            this.coverageId = coverageId;
      }

      public String getProviderName() {
            return providerName;
      }

      public void setProviderName(String providerName) {
            this.providerName = providerName;
      }

      public JGeometry getGeometry() {
            return geometry;
      }

      public void setGeometry(JGeometry geometry) {
            this.geometry = geometry;
      }
}
