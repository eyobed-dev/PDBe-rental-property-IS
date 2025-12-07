package cz.vut.fit.pdbe.xnuriey00.ui.map.layers;

import cz.vut.fit.pdbe.xnuriey00.model.School;
import cz.vut.fit.pdbe.xnuriey00.ui.map.MapPanel;

import java.awt.*;
import java.util.List;

public class SchoolLayer extends MapLayer {
      private List<School> areas;

      public SchoolLayer() {
            super("Schools");
      }

      public void setAreas(List<School> areas) {
            this.areas = areas;
      }

      @Override
      public void render(Graphics2D g, MapPanel map) {
            if (areas == null)
                  return;
            g.setColor(new Color(255, 153, 153, 50));

            for (School area : areas) {
                  try {
                        Shape shape = map.jGeometryToShape(area.getGeometry());
                        g.fill(shape);
                        g.draw(shape);
                  } catch (Exception e) {
                        // Ignore invalid geometries
                  }
            }
      }

      @Override
      public cz.vut.fit.pdbe.xnuriey00.model.SpatialEntity findEntityAt(java.awt.geom.Point2D p, MapPanel map) {
            if (areas == null)
                  return null;

            double tolerance = 5.0 / map.getScale();
            java.awt.geom.Rectangle2D clickArea = new java.awt.geom.Rectangle2D.Double(p.getX() - tolerance / 2,
                        p.getY() - tolerance / 2, tolerance, tolerance);

            for (int i = areas.size() - 1; i >= 0; i--) {
                  School area = areas.get(i);
                  try {
                        Shape shape = map.jGeometryToShape(area.getGeometry());
                        if (shape.intersects(clickArea)) {
                              return area;
                        }
                  } catch (Exception e) {
                        // Ignore
                  }
            }
            return null;
      }
}
