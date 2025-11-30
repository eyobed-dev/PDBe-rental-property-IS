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

            g.setColor(new Color(0, 0, 255, 50)); // Transparent Blue

            for (School area : areas) {
                  try {
                        Shape shape = map.jGeometryToShape(area.getGeometry());
                        g.fill(shape);
                        g.setColor(Color.BLUE);
                        g.draw(shape);
                        g.setColor(new Color(0, 0, 255, 50)); // Reset fill color
                  } catch (Exception e) {
                        // Ignore invalid geometries
                  }
            }
      }
}
