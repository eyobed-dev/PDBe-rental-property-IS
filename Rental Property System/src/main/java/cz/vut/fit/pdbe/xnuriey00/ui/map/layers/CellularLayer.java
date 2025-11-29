package cz.vut.fit.pdbe.xnuriey00.ui.map.layers;

import cz.vut.fit.pdbe.xnuriey00.model.CellularCoverage;
import cz.vut.fit.pdbe.xnuriey00.ui.map.MapPanel;

import java.awt.*;
import java.util.List;

public class CellularLayer extends MapLayer {
      private List<CellularCoverage> coverages;

      public CellularLayer() {
            super("Cellular");
      }

      public void setCoverages(List<CellularCoverage> coverages) {
            this.coverages = coverages;
      }

      @Override
      public void render(Graphics2D g, MapPanel map) {
            if (coverages == null)
                  return;

            g.setColor(new Color(0, 255, 0, 50));

            for (CellularCoverage coverage : coverages) {
                  try {
                        Shape shape = map.jGeometryToShape(coverage.getGeometry());
                        g.fill(shape);
                        g.setColor(Color.GREEN);
                        g.draw(shape);
                        g.setColor(new Color(0, 255, 0, 50));
                  } catch (Exception e) {
                        // Ignore invalid geometries
                  }
            }
      }
}
