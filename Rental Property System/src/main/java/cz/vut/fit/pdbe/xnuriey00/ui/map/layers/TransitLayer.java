package cz.vut.fit.pdbe.xnuriey00.ui.map.layers;

import cz.vut.fit.pdbe.xnuriey00.model.Transit;
import cz.vut.fit.pdbe.xnuriey00.ui.map.MapPanel;

import java.awt.*;
import java.util.List;

public class TransitLayer extends MapLayer {
      private List<Transit> stops;

      public TransitLayer() {
            super("Transit");
      }

      public void setStops(List<Transit> stops) {
            this.stops = stops;
      }

      @Override
      public void render(Graphics2D g, MapPanel map) {
            if (stops == null)
                  return;

            g.setColor(Color.ORANGE);

            for (Transit stop : stops) {
                  try {
                        Shape shape = map.jGeometryToShape(stop.getGeometry());
                        g.fill(shape);
                        g.setColor(Color.ORANGE);
                        g.draw(shape);
                        g.setColor(Color.ORANGE);
                  } catch (Exception e) {
                        // Ignore invalid geometries
                  }
            }
      }

      @Override
      public cz.vut.fit.pdbe.xnuriey00.model.SpatialEntity findEntityAt(java.awt.geom.Point2D p, MapPanel map) {
            if (stops == null)
                  return null;

            double tolerance = 5.0 / map.getScale();
            java.awt.geom.Rectangle2D clickArea = new java.awt.geom.Rectangle2D.Double(p.getX() - tolerance / 2,
                        p.getY() - tolerance / 2, tolerance, tolerance);

            for (int i = stops.size() - 1; i >= 0; i--) {
                  Transit stop = stops.get(i);
                  try {
                        Shape shape = map.jGeometryToShape(stop.getGeometry());
                        if (shape.intersects(clickArea)) {
                              return stop;
                        }
                  } catch (Exception e) {
                        // Ignore
                  }
            }
            return null;
      }
}
