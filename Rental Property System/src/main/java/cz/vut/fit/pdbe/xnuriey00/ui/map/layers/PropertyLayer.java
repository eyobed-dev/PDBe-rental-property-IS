package cz.vut.fit.pdbe.xnuriey00.ui.map.layers;

import cz.vut.fit.pdbe.xnuriey00.model.Property;
import cz.vut.fit.pdbe.xnuriey00.ui.map.MapPanel;
import oracle.spatial.geometry.JGeometry;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.List;

public class PropertyLayer extends MapLayer {
      private List<Property> properties;

      public PropertyLayer() {
            super("Properties");
      }

      public void setProperties(List<Property> properties) {
            this.properties = properties;
      }

      @Override
      public void render(Graphics2D g, MapPanel map) {
            if (properties == null)
                  return;

            double scale = map.getScale();

            for (Property prop : properties) {
                  JGeometry geom = prop.getJGeometry();
                  try {
                        Shape shape = map.jGeometryToShape(geom);

                        // Draw interior only for Polygons/Circles
                        if (geom.getType() == JGeometry.GTYPE_POLYGON
                                    || geom.getType() == JGeometry.GTYPE_MULTIPOLYGON) {
                              g.setColor(Color.LIGHT_GRAY);
                              g.fill(shape);
                        }

                        // Draw boundary (or the line itself)
                        g.setColor(Color.BLACK);
                        float strokeWidth = (float) (1.0 / scale);
                        g.setStroke(new BasicStroke(strokeWidth));
                        g.draw(shape);

                        // Draw Label
                        Rectangle2D bounds = shape.getBounds2D();
                        double labelX = bounds.getCenterX();
                        double labelY = bounds.getCenterY();

                        Font font = new Font("SansSerif", Font.BOLD, 12);
                        Font derivedFont = font.deriveFont((float) (12.0 / scale));
                        g.setFont(derivedFont);
                        g.setColor(Color.BLACK);
                        if (prop.getBuildingName() != null) {
                              g.drawString(prop.getBuildingName(), (float) labelX, (float) labelY);
                        }

                  } catch (Exception e) {
                        // Ignore
                  }
            }

      }

      public Property findPropertyAt(java.awt.geom.Point2D p, MapPanel map) {
            if (properties == null)
                  return null;

            double tolerance = 5.0 / map.getScale(); // 5 pixels tolerance
            java.awt.geom.Rectangle2D clickArea = new java.awt.geom.Rectangle2D.Double(p.getX() - tolerance / 2,
                        p.getY() - tolerance / 2, tolerance, tolerance);

            // Iterate in reverse to select top-most (last drawn) first
            for (int i = properties.size() - 1; i >= 0; i--) {
                  Property prop = properties.get(i);
                  JGeometry geom = prop.getJGeometry();
                  try {
                        Shape shape = map.jGeometryToShape(geom);
                        if (shape.intersects(clickArea)) {
                              return prop;
                        }
                  } catch (Exception e) {
                        // Ignore
                  }
            }
            return null;
      }
}
