package cz.vut.fit.pdbe.xnuriey00.ui.map.tools;

public enum GeometryType {
      POINT("Point"),
      LINE("Line"),
      POLYGON("Polygon"),
      CIRCLE("Circle"),
      MULTI_POINT("Multi Point"),
      MULTI_LINE("Multi Line"),
      MULTI_POLYGON("Multi Polygon");

      private final String label;

      GeometryType(String label) {
            this.label = label;
      }

      @Override
      public String toString() {
            return label;
      }
}
