package cz.vut.fit.pdbe.xnuriey00.ui.map.layers;

import cz.vut.fit.pdbe.xnuriey00.model.BikeRoute;
import cz.vut.fit.pdbe.xnuriey00.ui.map.MapPanel;

import java.awt.*;
import java.util.List;

/**
 * Renders bike routes as lines on the map.
 * 
 * @author eyobed-dev
 */
public class BikeRouteLayer extends MapLayer {
      private List<BikeRoute> routes;

      public BikeRouteLayer() {
            super("Bike Routes");
      }

      public void setRoutes(List<BikeRoute> routes) {
            this.routes = routes;
      }

      @Override
      public void render(Graphics2D g, MapPanel map) {
            if (routes == null)
                  return;

            // Use a distinct color for bike routes - bright green
            g.setColor(new Color(0, 200, 0));
            g.setStroke(new BasicStroke(3.0f)); // Thicker line for visibility

            for (BikeRoute route : routes) {
                  try {
                        Shape shape = map.jGeometryToShape(route.getGeometry());
                        g.draw(shape); // Draw the line
                  } catch (Exception e) {
                        // Ignore invalid geometries
                  }
            }

            // Reset stroke
            g.setStroke(new BasicStroke(1.0f));
      }

      @Override
      public cz.vut.fit.pdbe.xnuriey00.model.SpatialEntity findEntityAt(java.awt.geom.Point2D p, MapPanel map) {
            if (routes == null)
                  return null;

            double tolerance = 5.0 / map.getScale();
            java.awt.geom.Rectangle2D clickArea = new java.awt.geom.Rectangle2D.Double(p.getX() - tolerance / 2,
                        p.getY() - tolerance / 2, tolerance, tolerance);

            for (int i = routes.size() - 1; i >= 0; i--) {
                  BikeRoute route = routes.get(i);
                  try {
                        Shape shape = map.jGeometryToShape(route.getGeometry());
                        if (shape.intersects(clickArea)) {
                              return route;
                        }
                  } catch (Exception e) {
                        // Ignore
                  }
            }
            return null;
      }
}
