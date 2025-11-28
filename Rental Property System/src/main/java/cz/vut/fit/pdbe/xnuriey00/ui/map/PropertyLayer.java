package cz.vut.fit.pdbe.xnuriey00.ui.map;

import cz.vut.fit.pdbe.xnuriey00.model.Property;
import oracle.spatial.geometry.JGeometry;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.List;

public class PropertyLayer implements MapLayer {
      private List<Property> properties;

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

                        // Draw interior
                        g.setColor(new Color(0, 0, 255, 100));
                        g.fill(shape);

                        // Draw boundary
                        g.setColor(Color.BLUE);
                        float strokeWidth = (float) (2.0 / scale);
                        g.setStroke(new BasicStroke(strokeWidth));
                        g.draw(shape);

                        // Draw Label
                        Rectangle2D bounds = shape.getBounds2D();
                        double labelX = bounds.getCenterX();
                        double labelY = bounds.getCenterY();

                        Font font = new Font("SansSerif", Font.PLAIN, 12);
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
}
