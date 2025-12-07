package cz.vut.fit.pdbe.xnuriey00.ui.map.layers;

import cz.vut.fit.pdbe.xnuriey00.model.Cellular;
import cz.vut.fit.pdbe.xnuriey00.ui.map.MapPanel;

import java.awt.*;
import java.util.List;

public class CellularLayer extends MapLayer {
      private List<Cellular> coverages;

      public CellularLayer() {
            super("Cellular");
      }

      public void setCoverages(List<Cellular> coverages) {
            this.coverages = coverages;
      }

      @Override
      public void render(Graphics2D g, MapPanel map) {
            if (coverages == null)
                  return;

            g.setColor(new Color(109, 197, 250, 39));

            for (Cellular coverage : coverages) {
                  try {
                        Shape shape = map.jGeometryToShape(coverage.getGeometry());
                        g.fill(shape);
                        g.draw(shape);
                  } catch (Exception e) {
                        // Ignore invalid geometries
                  }
            }
      }

      @Override
      public cz.vut.fit.pdbe.xnuriey00.model.SpatialEntity findEntityAt(java.awt.geom.Point2D p, MapPanel map) {
            if (coverages == null)
                  return null;

            double tolerance = 5.0 / map.getScale();
            java.awt.geom.Rectangle2D clickArea = new java.awt.geom.Rectangle2D.Double(p.getX() - tolerance / 2,
                        p.getY() - tolerance / 2, tolerance, tolerance);

            for (int i = coverages.size() - 1; i >= 0; i--) {
                  Cellular coverage = coverages.get(i);
                  try {
                        Shape shape = map.jGeometryToShape(coverage.getGeometry());
                        if (shape.intersects(clickArea)) {
                              return coverage;
                        }
                  } catch (Exception e) {
                        // Ignore
                  }
            }
            return null;
      }
}
